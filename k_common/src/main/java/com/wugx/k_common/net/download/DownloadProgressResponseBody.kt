package com.wugx.k_utils.net.download

import com.hazz.kotlinmvp.rx.scheduler.SchedulerUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 * 下载文件responseBody
 */
class DownloadProgressResponseBody(val responseBody: ResponseBody, val progressListener: DownloadProgressListener) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    private var time1 = 0L

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(createSource(responseBody.source()))
        }
        return bufferedSource!!
    }

    private fun createSource(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                //300ms一次
                if (System.currentTimeMillis() - time1 > 300) {
                    //切换到主线程
                    Observable.just(totalBytesRead)
                            .compose(SchedulerUtils.ioToMain())
//                        .subscribe {
//                            progressListener.onProgress(it, responseBody.contentLength(), bytesRead == -1L)
//                        }
                            .subscribe(object : Observer<Long> {
                                lateinit var mDisposable: Disposable
                                override fun onComplete() {
                                    if (mDisposable != null) mDisposable.dispose()
                                }

                                override fun onSubscribe(d: Disposable) {
                                    mDisposable = d
                                }

                                override fun onNext(t: Long) {
                                    time1 = System.currentTimeMillis()
                                    progressListener.onProgress(t, responseBody.contentLength(), bytesRead == -1L)
                                    if (mDisposable != null) mDisposable.dispose()
                                }

                                override fun onError(e: Throwable) {
                                    if (mDisposable != null) mDisposable.dispose()
                                }
                            })
                }

//                progressListener.onProgress(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
                return bytesRead
            }
        }
    }
}
