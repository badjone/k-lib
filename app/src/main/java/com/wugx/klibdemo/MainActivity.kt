package com.wugx.klibdemo

import android.os.Bundle
import com.wugx.alarm_pro.net.HttpHelper
import com.wugx.k_common.base.BaseActivity
import com.wugx.k_common.util.utilcode.util.FileUtils
import com.wugx.k_common.util.utilcode.util.LogUtils
import com.wugx.k_utils.mvp.model.FileDownloadModel
import com.wugx.k_utils.net.Api
import com.wugx.k_utils.net.download.DownloadProgressListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {
    override fun initCreate(savedInstanceState: Bundle?) {

//        cl_main_content.visibility = View.GONE
//        showNoContentView(null)

//        PermissionApply.request("相机", object : PermissionApply.PermissionListener {
//            override fun success() {
//
//
//            }
//
//        }, PermissionConstants.GROUP_CAMERA)


        button.setOnClickListener {
            downFile()
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_main
    override fun isShowTitle(): Boolean = true
    override fun showBackIcon(): Boolean = false

    private fun downFile() {
        val downUrl = "http://192.144.137.174:8081/uploadFile/soft/version/1543800668298687447.apk"
        FileDownloadModel().downloadFile(this, downUrl, object : DownloadProgressListener {
            override fun onProgress(progress: Long, total: Long, done: Boolean) {
                val result = "下载 ${(100 * progress / total).toInt()} %"
                LogUtils.d("下载 ${(100 * progress / total).toInt()} %  >>${Thread.currentThread()}")
                textView.text = result
            }

            override fun downComplete(file: File) {
                val result = "下载完成 ${FileUtils.isFileExists(file)} >>>${FileUtils.getFileSize(file)}"
                LogUtils.d("下载完成 ${FileUtils.isFileExists(file)} >>>${FileUtils.getFileSize(file)}>>${Thread.currentThread()}")
                textView.text = result
            }

            override fun downError(reason: String) {
                LogUtils.d("下载异常 $reason  >>${Thread.currentThread()}")
                textView.text = reason
            }
        })
    }

}
