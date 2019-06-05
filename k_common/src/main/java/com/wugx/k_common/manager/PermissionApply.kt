package com.wugx.alarm_pro.utils

import android.annotation.SuppressLint
import com.afollestad.materialdialogs.MaterialDialog
import com.wugx.k_common.R
import com.wugx.k_common.util.utilcode.util.ActivityUtils
import com.wugx.k_common.util.utilcode.util.PermissionUtils
import com.wugx.k_common.util.utilcode.util.ToastUtils
import com.wugx.k_common.util.utilcode.util.Utils

/**
 * 权限申请 在此申请一些公共权限
 *
 * @author Wugx
 * @date 2018/11/8
 */
object PermissionApply {

    @SuppressLint("WrongConstant")
    @JvmStatic
    fun request(perms: Array<String>,permsDesc: String, listener: PermissionListener) {
        PermissionUtils.permission(*perms)
            .rationale { shouldRequest -> shouldRequest.again(true) }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    listener.success()
                }

                override fun onDenied(permissionsDeniedForever: List<String>?, permissionsDenied: List<String>?) {
                    if (permissionsDenied != null && permissionsDenied.isNotEmpty()) {
                        ToastUtils.showShort(
                            String.format(
                                Utils.getApp().getString(R.string.permission_denied_tips),
                                permsDesc
                            )
                        )
                    }
                    if (permissionsDeniedForever != null && permissionsDeniedForever.isNotEmpty()) {
                        val topActivity = ActivityUtils.getTopActivity()
                        if (topActivity != null) {
                            MaterialDialog(topActivity)
                                .message(
                                    text = String.format(
                                        Utils.getApp().getString(R.string.permission_refuse_tips),
                                        permsDesc
                                    )
                                )
                                .cancelOnTouchOutside(false)
                                .negativeButton(R.string.cancel)
                                .positiveButton(R.string.txt_set) {
                                    PermissionUtils.launchAppDetailsSettings()
                                }.show()
                        }
                    }
                }
            })
            .request()
    }

    interface PermissionListener {
        fun success()

    }

}
