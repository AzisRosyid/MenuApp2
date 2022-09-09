package com.example.menuapp.Api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FileRepository {

    private val api by lazy { ApiRetrofit().apiEndPoint }

    fun uploadImage(file: File): Boolean {
        return try {
            api.createMenu(
                "Hello".toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                "1000".toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                "200".toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                "100".toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                image = MultipartBody.Part
                    .createFormData("image", file.name, file.asRequestBody())
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}