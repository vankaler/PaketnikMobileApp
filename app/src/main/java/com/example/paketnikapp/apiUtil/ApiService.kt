package com.example.paketnikapp.apiUtil

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("your/video/endpoint") // ni se backend poti
    fun sendVideo(@Part video: MultipartBody.Part): Call<Void>
}
