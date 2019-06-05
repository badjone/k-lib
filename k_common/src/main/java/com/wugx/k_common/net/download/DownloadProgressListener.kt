package com.wugx.k_utils.net.download

import java.io.File

/**
 * 下载文件 listener
 */
interface DownloadProgressListener {

    /**
     * @param progress     已经下载或上传字节数
     * @param total        总字节数
     * @param done         是否完成
     */
    fun onProgress(progress: Long, total: Long, done: Boolean)

    /**
     * 下载成功
     */
    fun downComplete(file:File)

    /**
     * 下载异常
     */
    fun downError(reason: String)

}
