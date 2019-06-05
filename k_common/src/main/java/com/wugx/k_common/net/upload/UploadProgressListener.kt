package com.wugx.alarm_pro.net.upload

/**
 * @author wugx
 * @data 2018/6/8.
 */

interface UploadProgressListener {
    /**
     * 上传进度
     * @param currentBytesCount
     * @param totalBytesCount
     */
    fun onProgress(currentBytesCount: Long, totalBytesCount: Long)
}
