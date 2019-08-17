package com.wugx.k_utils.net

import com.wugx.alarm_pro.net.interceptor.CommonParamsInterceptor
import com.wugx.alarm_pro.net.interceptor.HttpCacheInterceptor
import com.wugx.k_common.util.utilcode.util.Utils
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 *
 *
 *@author Wugx
 *@date   2018/12/20
 */
object RetrofitManager {
    private const val DEFAULT_TIMEOUT = 15L

    fun getRetrofit(baseUrl: String, map: Map<String, String>): Retrofit {
        // 获取retrofit的实例
        return Retrofit.Builder()
            .baseUrl(baseUrl)  //自己配置
            .client(getOkHttpClient(addInterceptorParams(map)).build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getRetrofit(baseUrl: String, httpClient: OkHttpClient, map: Map<String, String>): Retrofit {
        // 获取retrofit的实例
        return Retrofit.Builder()
            .baseUrl(baseUrl)  //自己配置
            .client(httpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getOkHttpClient(interceptor: CommonParamsInterceptor): OkHttpClient.Builder {
        val cacheFile = File(Utils.getApp().cacheDir, "cache")
        val cache = Cache(cacheFile, (1024 * 1024 * 100).toLong())

        return OkHttpClient.Builder()
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
//                    .addInterceptor(HttpHeaderInterceptor())
            .addNetworkInterceptor(HttpCacheInterceptor())
            //https认证 如果要使用https且为自定义证书,自行配制证书。
//                    .sslSocketFactory(SslContextFactory.getSSLSocketFactoryForTwoWay())
//                    .hostnameVerifier(SafeHostnameVerifier())
            .cache(cache)
    }

    fun addInterceptorParams(map: Map<String, String>): CommonParamsInterceptor {
        val builder = CommonParamsInterceptor.Builder()
        for (item in map) {
            builder.addBodyParams(item.key, item.value)
        }
        return builder.build()
    }

//    /**
//     * 设置公共参数
//     */
//    @SuppressLint("MissingPermission")
//    private fun addQueryParameterInterceptor(): Interceptor {
//        val comInterceptor = CommonParamsInterceptor.Builder()
////                    .addHeaderParams(key,vaule)  //添加header
////                    .addBodyParams(key,vaule) //添加body
//                .addBodyParams("IMEI", PhoneUtils.getIMEI())
//                .addBodyParams("loginType", "1")
//                .addBodyParams("loginDevice", "1")
//                .addBodyParams("deviceid", DeviceUtils.getAndroidID())
//                .addBodyParams("random", Math.random().toString())
//                //切换中英文
//                .addBodyParams("lang", "1")
//
//        val token = SPUtils.getInstance().getString(X_TOKEN)
//        if (!StringUtils.isEmpty(token)) comInterceptor.addBodyParams("token", token)
//        val userId = SPUtils.getInstance().getString(X_USERID)
//        if (!StringUtils.isEmpty(userId)) comInterceptor.addBodyParams("userid", userId)
//        return comInterceptor.build()
//    }
}