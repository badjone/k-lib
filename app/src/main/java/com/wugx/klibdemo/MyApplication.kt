package com.wugx.klibdemo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.wugx.k_common.KCommon
import com.wugx.k_common.util.utilcode.util.Utils

//import com.wugx.k_lib.base.BaseApplication

/**
 *
 * @Author badjone
 * @Date 2019/5/31 15:48
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        KUtils.init(this)
//            .initNetParams("", mapOf())

//        Utils.init(this)

        KCommon.create(this).initNetParams("http://192.144.137.174:8081", mapOf())

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}