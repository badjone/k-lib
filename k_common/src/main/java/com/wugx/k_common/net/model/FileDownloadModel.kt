package com.wugx.k_utils.mvp.model

import com.hazz.kotlinmvp.rx.scheduler.SchedulerUtils
import com.wugx.alarm_pro.net.HttpHelper
import com.wugx.alarm_pro.utils.PermissionApply
import com.wugx.k_common.KCommon
import com.wugx.k_common.util.utilcode.constant.PermissionConstants
import com.wugx.k_common.util.utilcode.util.FileIOUtils
import com.wugx.k_common.util.utilcode.util.FileUtils
import com.wugx.k_common.util.utilcode.util.SDCardUtils
import com.wugx.k_utils.base.IBaseView
import com.wugx.k_utils.net.KApi
import com.wugx.k_utils.net.RetrofitManager
import com.wugx.k_utils.net.download.DownloadProgressListener
import com.wugx.k_utils.net.download.DownloadProgressResponseBody
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import okhttp3.Interceptor
import okhttp3.ResponseBody
import java.io.File

/**
 *文件下载 apk
 *
 *@author Wugx
 *@date   2018/12/22
 */
class FileDownloadModel {

    companion object {
        /**
         * 下载文件名
         */
        const val down_file_name = "zzz.apk"
    }

    fun downloadFile(p: IBaseView, url: String, listener: DownloadProgressListener) {
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
                        )
                            .build()
                    })
                    HttpHelper.createRetrofit(builder.build(), KApi::class.java)
                        .downloadFile(url)
                        .doOnNext(getConsumer(listener))
                        .compose(SchedulerUtils.ioToMain())
                        .compose(p.bindToLife())
                        .subscribe(object : Observer<ResponseBody> {
                            override fun onComplete() {

                            }

                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(t: ResponseBody) {
                                //下载完成
                            }

                            override fun onError(e: Throwable) {
                                listener.downError(e.localizedMessage)
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