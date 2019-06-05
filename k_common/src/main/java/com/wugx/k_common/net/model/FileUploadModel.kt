package com.wugx.k_utils.mvp.model

import com.wugx.alarm_pro.net.HttpHelper
import com.wugx.alarm_pro.net.upload.UploadProgressListener
import com.wugx.alarm_pro.net.upload.UploadProgressRequestBody
import com.wugx.k_common.util.utilcode.util.LogUtils
import com.wugx.k_utils.base.IBaseView
import com.wugx.k_utils.net.Api
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 文件上传 model
 *
 *@author Wugx
 *@date   2018/12/21
 */
class FileUploadModel {
    /**
     * fileKey 文件上传的key
     * params  普通参数
     */
    fun <T> uploadFile(p: IBaseView, url: String, fileKey: String, file: File,
                       params: Map<String, String>, listener: UpFileProgressListener<T>) {
        //        final MaterialDialog dialog = new DialogUtils().showFileProgressBar(ba);
        listener.start()
        val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
        //一般参数
        for (paramKey in params.keys) {
            builder.addFormDataPart(paramKey, params[paramKey]!!)
        }
//        builder.addFormDataPart("fileType", fileKey)

        LogUtils.d("上传文件参数>>>$params")
        //文件
//        val requestBody  = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        //显示上传进度
        val uploadProgressRequestBody = UploadProgressRequestBody(requestBody, object : UploadProgressListener {
            var time = System.currentTimeMillis()
            override fun onProgress(currentBytesCount: Long, totalBytesCount: Long) {
                //100毫秒更新一次
                if (System.currentTimeMillis() - time > 100) {
                    val progress = (currentBytesCount.toFloat() / totalBytesCount.toFloat() * 100).toInt()
                    listener.progress(progress)
                    time = System.currentTimeMillis()
                }
                if (currentBytesCount == totalBytesCount) {
                    listener.progress(100)
                }
            }
        })
        builder.addFormDataPart(fileKey, file.name, uploadProgressRequestBody)
        val partList = builder.build().parts()
        HttpHelper.createRetrofit(Api::class.java)
                .uploadFile(url, partList)
                .subscribeOn(Schedulers.io())
                .compose(p.bindToLife())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: String) {
                        listener.success(t as T)
                    }

                    override fun onError(e: Throwable) {
                        listener.failure()
                    }
                })
    }

    interface UpFileProgressListener<T> {
        fun start()

        fun progress(progress: Int)

        fun failure()

        fun success(t: T)
    }
}