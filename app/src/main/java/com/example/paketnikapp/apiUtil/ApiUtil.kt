package com.example.paketnikapp.apiUtil

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ApiUtil {

     private val BASE_URL = "http://" + serverIP + ":3005/" // Update with your server IP

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(160, TimeUnit.SECONDS)
        .readTimeout(160, TimeUnit.SECONDS)
        .writeTimeout(160, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun uploadVideo(videoFile: File, userId: RequestBody, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val requestFile = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, requestFile)

        Log.d("ApiUtil", "Uploading video: ${videoFile.name}, Client ID: ${userId}")

        val call = apiService.uploadVideo(videoPart, userId)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("ApiUtil", "Video uploaded successfully")
                    onSuccess()
                } else {
                    val errorMessage = "Failed to upload video: ${response.code()} - ${response.message()}"
                    Log.e("ApiUtil", errorMessage)
                    onFailure(Exception(errorMessage))
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Log.e("ApiUtil", "Error uploading video", t)
                onFailure(t)
            }
        })
    }

    fun login(
        email: String,
        password: String,
        fcmToken: String,
        onSuccess: (ApiResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.login(LoginRequest(email, password, fcmToken))
        call.enqueue(object : retrofit2.Callback<ApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<ApiResponse>,
                response: retrofit2.Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Login failed: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: (ApiResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.register(RegisterRequest(firstName, lastName, email, password))
        call.enqueue(object : retrofit2.Callback<ApiResponse> {
            override fun onResponse(
                call: retrofit2.Call<ApiResponse>,
                response: retrofit2.Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Registration failed: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun logout(onSuccess: (ApiResponse) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.logout()
        call.enqueue(object : retrofit2.Callback<ApiResponse> {
            override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Logout failed: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getAllClients(onSuccess: (ResponseBody) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.getAllClients()
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Logout failed: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })

    }

    fun getClientById(
        id: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.getClientById(id)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get client by id: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getAllRooms(onSuccess: (ResponseBody) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.getAllRooms()
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get all rooms: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getRoomById(
        id: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.getRoomById(id)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get room by id: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getAllStaff(onSuccess: (ResponseBody) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.getAllStaff()
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                        Log.e("Image", it.string())
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get image: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getStaffById(
        id: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.getStaffById(id)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get staff by id: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getAllInfo(onSuccess: (ResponseBody) -> Unit, onFailure: (Throwable) -> Unit) {
        val call = apiService.getAllInfo()
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get all info: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun accessPackageContract(
        client: String,
        code: Int,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.accessPackageContract(AccessPackageContractBody(client, code))
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to access package contract: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun createPackageLog(
        code: Int,
        openedBy: String,
        type: Boolean,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.createPackageLog(CreatePackageLogBody(code, openedBy, type))
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to create package log: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getPackageLogById(
        id: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.getPackageLogById(id)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get package log by id: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    fun getClientHasRoomsById(
        clientId: String,
        onSuccess: (ResponseBody) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val call = apiService.getClientHasRoomsById(clientId)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onFailure(Exception("Empty response body"))
                } else {
                    onFailure(Exception("Failed to get client has rooms by id: ${response.code()}"))
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
