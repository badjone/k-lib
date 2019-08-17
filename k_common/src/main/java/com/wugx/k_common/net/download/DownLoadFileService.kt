package com.wugx.k_utils.net.download

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import com.hazz.kotlinmvp.rx.scheduler.SchedulerUtils
import com.wugx.alarm_pro.net.HttpHelper
import com.wugx.alarm_pro.utils.PermissionApply
import com.wugx.k_common.KCommon
import com.wugx.k_common.R
import com.wugx.k_common.util.utilcode.constant.PermissionConstants
import com.wugx.k_common.util.utilcode.util.*
import com.wugx.k_utils.net.KApi
import com.wugx.k_utils.net.RetrofitManager
import com.wugx.k_utils.utils.NotificationUtil
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import okhttp3.Interceptor
import okhttp3.ResponseBody
import java.io.File

/**
 * 下载文件
 *
 *@author Wugx
 *@date   2018/12/22
 */
class DownLoadFileService : IntentService("kUtils_down_file") {
    private var downLoadUrl: String? = null

    companion object {
        var isWorking: Boolean = false
        //下载文件名称
        var down_file_name = "zzz.apk"

        fun startDown(url: String) {
            if (isWorking) {
                ToastUtils.showShort("正在更新中,请稍后再试")
                return
            }
//            val testDownUrl = "http://192.144.137.174:8081/uploadFile/soft/version/1543800668298687447.apk"
            val intent = Intent(Utils.getApp(), DownLoadFileService::class.java)
            intent.putExtra("downLoadUrl", url)
            Utils.getApp().startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            downLoadUrl = intent.getStringExtra("downLoadUrl")
        }
        if (!isWorking) {
            //正在下载中...
            downLoadFile(downLoadUrl!!, object : DownloadProgressListener {
                override fun onProgress(progress: Long, total: Long, done: Boolean) {
                    LogUtils.d("onProgress>>>$progress >>$isWorking")
                    if (done) {
                        isWorking = false
                        NotificationUtil.createNotificationManager().cancel(101)
                    } else {
                        isWorking = true
                        val nProgress = progress * 100 / total
                        val buildNotification = NotificationUtil.buildNotification(
                            "download_file",
                            R.mipmap.icon_notification_small, "版本更新", "正在更新中...", null
                        )
                        buildNotification.setProgress(100, nProgress.toInt(), false)
                        NotificationUtil.createNotificationManager().notify(101, buildNotification.build())
                    }
                }

                override fun downComplete(file: File) {
                    LogUtils.d("DownLoadFileService downComplete ${file.path}")
                    NotificationUtil.createNotificationManager().cancel(101)
                    //install apk
                    AppUtils.installApp(file)
                    isWorking = false
                    //停止服务
                    stopSelf()
                }

                override fun downError(reason: String) {
                    LogUtils.d("DownLoadFileService downError $reason")
                    NotificationUtil.createNotificationManager().cancel(101)
                    isWorking = false
                }
            })
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun downLoadFile(url: String, listener: DownloadProgressListener) {
        PermissionApply.request(
            arrayOf(PermissionConstants.STORAGE),
            "文件读写",
            object : PermissionApply.PermissionListener {
                override fun success() {
                    val builder = RetrofitManager.getOkHttpClient(RetrofitManager.addInterceptorParams(KCommon.map))
                    //添加拦截器，自定义ResponseBody，添加下载进度
                    builder.networkInterceptors().add(Interceptor { chain ->
                        val originalResponse = chain.proceed(chain.request())
                        originalResponse.newBuilder().body(
                            DownloadProgressResponseBody(originalResponse.body()!!, listener)
                        ).build()
                    })
                    HttpHelper.createRetrofit(builder.build(), KApi::class.java)
                        .downloadFile(url)
                        .doOnNext(getConsumer(listener))
                        .compose(SchedulerUtils.ioToMain())
                        .subscribe(object : Observer<ResponseBody> {
                            lateinit var mDisposable: Disposable
                            override fun onComplete() {
                            }

                            override fun onSubscribe(d: Disposable) {
                                mDisposable = d
                            }

                            override fun onNext(t: ResponseBody) {
                                //下载完成
                                mDisposable.dispose()
                            }

                            override fun onError(e: Throwable) {
                                listener.downError(e.localizedMessage)
                                mDisposable.dispose()
                            }
                        })
                }
            })
    }

    /**
     * 存储文件到 sdCard
     */
    private fun getConsumer(listener: DownloadProgressListener): Consumer<ResponseBody> {
        return Consumer { responseBody ->
            //sdCard路径
            val sdPath = SDCardUtils.getSDCardPathByEnvironment()
            val file = File(sdPath, down_file_name)
            FileUtils.createFileByDeleteOldFile(file)
            //写入文件
            Observable.just(responseBody.byteStream())
                .map {
                    //子线程写入文件
                    FileIOUtils.writeFileFromIS(file, responseBody.byteStream())
                }
                .compose(SchedulerUtils.ioToMain())
                .subscribe {
                    //回调写入文件结果
                    if (it) listener.downComplete(file) else listener.downError("写入文件异常")
                }
        }
    }
}