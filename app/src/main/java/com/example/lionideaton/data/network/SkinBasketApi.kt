package com.example.lionideaton.data.network

import com.example.lionideaton.data.model.FoodImageAnalysisResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface SkinBasketApi {
    // 안드로이드가 이미 하드코딩한 구 계약 — 봉투 없이 이 shape 그대로. 바꾸지 말 것
    // (skinbasket-backend/app/routers/ai.py 상단 주석 참고).
    @Multipart
    @POST("ai/food-image")
    suspend fun analyzeFoodImage(@Part image: MultipartBody.Part): FoodImageAnalysisResponse

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequest): Envelope<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Envelope<AuthResponse>

    @PATCH("users/me")
    suspend fun updateMe(@Body body: UpdateMeRequest): Envelope<AuthUser>

    @POST("users/me/constraints")
    suspend fun addConstraint(@Body body: ConstraintRequest): Envelope<Any>

    @GET("foods")
    suspend fun searchFoods(@Query("q") query: String, @Query("limit") limit: Int = 5): Envelope<List<BackendFoodOut>>

    @POST("foods")
    suspend fun createFood(@Body body: FoodCreateRequest): Envelope<BackendFoodOut>

    @POST("meals")
    suspend fun registerMeal(@Body body: MealRequest): Envelope<MealLogOut>

    @GET("meals")
    suspend fun getMeals(): Envelope<List<MealLogOut>>
}
