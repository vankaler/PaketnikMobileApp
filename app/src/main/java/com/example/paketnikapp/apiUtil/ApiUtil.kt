package com.example.paketnikapp.apiUtil

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object ApiUtil {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:3001/") // ni se def
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun sendVideo(videoFile: File, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val requestFile = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", videoFile.name, requestFile)

        val call = apiService.sendVideo(body)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(Exception("Failed to send video: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
