package com.wugx.k_utils.net

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 *
 *
 *@author Wugx
 *@date   2018/12/20
 */
interface KApi {






    /**
     * 单文件上传 方法一
     *
     * @param partList
     * @return
     */
    @Multipart
    @POST
    fun uploadFile(@Url url: String, @Part partList: List<MultipartBody.Part>): Observable<String>

    /**
     * 下载文件
     */
    @Streaming
    @GET
    fun downloadFile(/*@Header("Range") range: String,*/ @Url url: String): Observable<ResponseBody>


    @FormUrlEncoded
    @POST
    abstract fun postData(@Url url: String, @FieldMap params: Map<String, Any>): Observable<String>

    //add shibo.zheng start
    @POST
    abstract fun postBody(@Url url: String, @Body body: RequestBody): Observable<String>
    //add shibo.zheng end

    @GET
    abstract fun getData(@Url url: String, @QueryMap params: Map<String, Any>): Observable<String>


    @FormUrlEncoded
    @POST
    abstract fun postDataForBoolean(@Url url: String, @FieldMap params: Map<String, Any>): Observable<Boolean>

    @GET
    abstract fun getDataForBoolean(@Url url: String, @QueryMap params: Map<String, Any>): Observable<Boolean>

    @FormUrlEncoded
    @POST
    abstract fun postUrlForQuery(@Url url: String, @QueryMap paramsn: Map<String, Any>, @FieldMap body: Map<String, Any>): Observable<String>

    @Multipart
    @POST
    abstract fun uploadfile(@Url url: String, @QueryMap params: Map<String, Any>, @PartMap body: Map<String, RequestBody>): Observable<String>


}