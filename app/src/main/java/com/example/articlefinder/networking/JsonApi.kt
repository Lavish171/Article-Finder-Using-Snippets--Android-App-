package com.example.articlefinder.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JsonApi {

    //http://20.185.230.90:5002/api/

    @Headers( value = ["Accept: application/json",
        "Content-type:application/json"])
    //@Headers("Accept: application/json")

    @POST("files")
    fun sendUserIdAndFileId(
        @Body retrofitPostUserIdAndPdfId: String
    ):Call<RetrofitPostUserIdAndPdfId>
}