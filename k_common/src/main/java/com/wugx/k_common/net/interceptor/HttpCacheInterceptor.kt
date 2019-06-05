package com.wugx.alarm_pro.net.interceptor


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpCacheInterceptor : Interceptor {
    //  配置缓存的拦截器
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //无缓存,进行缓存
        return chain.proceed(chain.request()).newBuilder()
                .removeHeader("Pragma")
                //对请求进行最大60秒的缓存
                .addHeader("Cache-Control", "max-age=60")
                .build()
    }
}
