package com.wugx.alarm_pro.net.interceptor

import com.wugx.k_common.util.utilcode.util.LogUtils
import com.wugx.k_common.util.utilcode.util.NetworkUtils
import com.wugx.k_common.util.utilcode.util.SpanUtils
import okhttp3.*
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 公共拦截器
 *
 * @author wugx
 * @data 2018/6/11.
 */

class CommonParamsInterceptor : Interceptor {


    private val bodyParms = HashMap<String, String>()
    private val headerParamsMap = HashMap<String, String>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        //获取到请求
        var oldRequest = chain.request()
        val startTime = System.nanoTime()
        //获取请求的方式
        val method = oldRequest.method()
        //获取请求的路径
        val oldUrl = oldRequest.url().toString()

        var newBuilder: Request.Builder = oldRequest.newBuilder()
        newBuilder.method(oldRequest.method(), oldRequest.body())
        //添加公共参数,添加到header中
        if (headerParamsMap.size > 0) {
            for ((key, value) in headerParamsMap) {
                newBuilder.header(key, value)
            }
        }

        var params = ""
        if ("GET" == method) {
            val stringBuilder = showGetParm(oldUrl, bodyParms)
            params = stringBuilder.toString().replace(oldUrl, "")
            val newUrl = stringBuilder.toString()//新的路径
            //拿着新的路径重新构建请求
            newBuilder = newBuilder
                    .url(newUrl)
        } else if ("POST" == method) {
            //先获取到老的请求的实体内容
            val oldRequestBody = oldRequest.body()//....之前的请求参数,,,需要放到新的请求实体内容中去
            //如果请求调用的是上面doPost方法
            if (oldRequestBody is FormBody) {
                val oldBody = oldRequestBody as FormBody?
                //构建一个新的请求实体内容
                val builder = FormBody.Builder()
                val sb = showPostParms(bodyParms, oldBody!!, builder)
                val newBody = builder.build()//新的请求实体内容

                params = sb.toString()
                //构建一个新的请求
                newBuilder = newBuilder
                        .url(oldUrl)
                        .post(newBody)
            } else if (oldRequestBody is MultipartBody) {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                val parts = oldRequestBody.parts()
                params = showMultiParms(bodyParms, builder, parts)

                newBuilder = newBuilder.url(oldUrl).post(builder.build())
            }
        }
        // cache配置
        val newRequest: Request
        if (NetworkUtils.isConnected()) {
            //有网络,检查10秒内的缓存
            newRequest = newBuilder
                    .cacheControl(CacheControl.Builder()
                            .maxAge(10, TimeUnit.SECONDS)
                            .build())
                    .build()
        } else {
            //无网络,检查30天内的缓存,即使是过期的缓存
            newRequest = chain.request().newBuilder()
                    .cacheControl(CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(30, TimeUnit.DAYS)
                            .build())
                    .build()
        }

        val response = chain.proceed(newRequest)
        createLog(response, newRequest, params, startTime)
        return response
    }

    /**
     * 创建log日志格式
     */
    private fun createLog(response: Response, newRequest: Request, params: String, startTime: Long) {
        val endTime = System.nanoTime()
        //返回数据...
        val source = response.body()!!.source()
        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer()
        val spanUtils = SpanUtils()
        spanUtils.apply {
            appendLine("method ->${newRequest.method()}")
            appendLine("network code->${response.code()}")
            appendLine("paramsStr-> [$params]")
            appendLine("time-> ${String.format(Locale.getDefault(), "%.1fms", (endTime - startTime) / 1e6)}")
            appendLine("url-> ${URLDecoder.decode(newRequest.url().toString(), "UTF-8")}")
            appendLine("request headers-> ${newRequest.headers()}")
            if (buffer!!.size() <= 400) {
                val bufferResult = buffer.clone().readString(Charset.forName("UTF-8"))
                appendLine("call-data-> $bufferResult")
            } else {
                appendLine("call-data->返回数据太长，省略显示^-^")
            }
        }
        //log打印网络请求信息
        LogUtils.w(spanUtils.create().toString())
    }

    /********************************获取几种请求方式的参数 */
    private fun showMultiParms(bodyParms: Map<String, String>?, builder: MultipartBody.Builder, parts: List<MultipartBody.Part>): String {
        val parms: String
        val sb = StringBuilder()
        if (bodyParms != null) {
            for ((key, value) in bodyParms) {
                builder.addFormDataPart(key, value)
                sb.append(key).append("=").append(value).append("&")
            }
        }
        //文件part
        for (i in parts.indices) {
            builder.addPart(parts[i]).build()
        }
        //删掉最后一个&符号
        if (sb.indexOf("&") != -1) {
            sb.deleteCharAt(sb.lastIndexOf("&"))
        }
        parms = sb.toString()
        return parms
    }

    private fun showPostParms(bodyParms: Map<String, String>, oldBody: FormBody, builder: FormBody.Builder): StringBuilder {
        val sb = StringBuilder()
        //1.添加老的参数
        for (i in 0 until oldBody.size()) {
            builder.add(oldBody.name(i), oldBody.value(i))
            sb.append(oldBody.name(i)).append("=").append(oldBody.value(i)).append("&")
        }

        //2.添加公共参数
        for ((key, value) in bodyParms) {
            builder.add(key, value)
            sb.append(key).append("=").append(value).append("&")
        }
        return sb
    }

    private fun showGetParm(oldUrl: String, bodyParms: Map<String, String>): StringBuilder {
        val stringBuilder = StringBuilder()//创建一个stringBuilder
        stringBuilder.append(oldUrl)
        if (oldUrl.contains("?")) {
            //?在最后面....2类型
            if (oldUrl.indexOf("?") == oldUrl.length - 1) {

            } else {
                //3类型...拼接上&
                stringBuilder.append("&")
            }
        } else {
            //不包含? 属于1类型,,,先拼接上?号
            stringBuilder.append("?")
        }
        //添加公共参数....
        for ((key, value) in bodyParms) {
            //拼接
            stringBuilder.append(key)
                    .append("=")
                    .append(value)
                    .append("&")
        }
        //删掉最后一个&符号
        if (stringBuilder.indexOf("&") != -1) {
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("&"))
        }
        return stringBuilder
    }


    class Builder {
        private val commonParamsInterceptor by lazy {
            CommonParamsInterceptor()
        }

        fun addHeaderParams(key: String, value: String): CommonParamsInterceptor.Builder {
            commonParamsInterceptor.headerParamsMap[key] = value
            return this
        }

        fun addBodyParams(key: String, value: String): CommonParamsInterceptor.Builder {
            commonParamsInterceptor.bodyParms[key] = value
            return this
        }

        fun build(): CommonParamsInterceptor {
            return commonParamsInterceptor
        }
    }

}

