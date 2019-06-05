package com.wugx.k_utils.net

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 *
 *
 *@author Wugx
 *@date   2018/12/20
 */
interface Api {

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


}