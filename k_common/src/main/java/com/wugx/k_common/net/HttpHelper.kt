package com.wugx.alarm_pro.net

import com.wugx.k_common.KCommon
import com.wugx.k_common.util.utilcode.util.ToastUtils
import com.wugx.k_utils.net.RetrofitManager
import okhttp3.OkHttpClient

/**
 * @author Wugx
 * @date 2018/11/9
 */
object HttpHelper {

    fun <T> createRetrofit(cls: Class<T>): T {
        checkNotNull(KCommon.base_url) {
            ToastUtils.showShort("initNetParams 必须被初始化")
        }
        return RetrofitManager.getRetrofit(KCommon.base_url, KCommon.map).create(cls)
    }


    fun <T> createRetrofit(okHttpClient: OkHttpClient, cls: Class<T>): T {
        return RetrofitManager.getRetrofit(KCommon.base_url, okHttpClient, KCommon.map).create(cls)
    }


}
