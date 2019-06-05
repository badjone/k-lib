package com.wugx.alarm_pro.net.upload

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * 上传下载进度条body
 *
 * @author wugx
 * @data 2018/6/8.
 */

class UploadProgressRequestBody(//实际的待包装请求体
        private val requestBody: RequestBody, //进度回调接口
        private val progressListener: UploadProgressListener) : RequestBody() {

    private var mCountingSink: CountingSink? = null

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    /**
     * 重写进行写入
     *
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        mCountingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(mCountingSink!!)
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()

    }

    internal inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val length = contentLength()
            //            LogUtils.d("上传进度>>" + bytesWritten + ">>" + contentLength() + ">>" + (bytesWritten == contentLength()));
            //切换为主线程

            Observable
                    .just(bytesWritten)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { progressListener.onProgress(bytesWritten, contentLength()) }

        }
    }
}
