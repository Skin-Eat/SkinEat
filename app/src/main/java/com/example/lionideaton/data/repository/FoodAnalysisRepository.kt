package com.example.lionideaton.data.repository

import android.graphics.Bitmap
import com.example.lionideaton.data.network.NetworkModule
import com.example.lionideaton.data.network.SkinBasketApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class FoodAnalysisRepository(private val api: SkinBasketApi = NetworkModule.api) {

    suspend fun analyzeFoodPhoto(bitmap: Bitmap): Result<List<String>> = runCatching {
        api.analyzeFoodImage(bitmap.toImagePart()).candidates
    }

    private fun Bitmap.toImagePart(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val requestBody = stream.toByteArray().toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", "food.jpg", requestBody)
    }
}
