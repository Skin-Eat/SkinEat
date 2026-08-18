package com.example.lionideaton.data.network

// 백엔드 공통 응답 봉투: {success, data, error} (skinbasket-backend/app/core/envelope.py).
// /ai/food-image만 이 봉투 없이 예전 shape 그대로라 FoodImageAnalysisResponse는 안 씀.
data class ApiError(val code: String, val message: String)
data class Envelope<T>(val success: Boolean, val data: T?, val error: ApiError?)

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val skinType: String? = null,
    val concerns: List<String> = emptyList(),
    val photoConsent: Boolean = false
)

data class LoginRequest(val email: String, val password: String)

data class AuthUser(
    val id: String,
    val nickname: String,
    val skinType: String?,
    val concerns: List<String>,
    val photoConsent: Boolean
)

data class AuthResponse(val accessToken: String, val user: AuthUser)

data class UpdateMeRequest(
    val nickname: String? = null,
    val skinType: String? = null,
    val concerns: List<String>? = null,
    val photoConsent: Boolean? = null
)

// ConstraintType 값은 백엔드가 소문자(allergy/dislike)를 씀.
data class ConstraintRequest(val type: String, val ingredientName: String)

data class BackendFoodOut(
    val id: Int,
    val name: String,
    val servingG: Int,
    val energyKcal: Double
)

data class FoodCreateRequest(
    val name: String,
    val servingG: Int,
    val energyKcal: Double,
    val carbG: Double = 0.0,
    val sugarG: Double = 0.0,
    val proteinG: Double = 0.0,
    val fatG: Double = 0.0
)

data class MealItemRequest(val foodId: Int, val portionRatio: Double, val isAiDetected: Boolean = false)

data class MealRequest(
    val eatenAt: String? = null,
    val mealType: String? = null,
    val photoUrl: String? = null,
    val items: List<MealItemRequest>
)

// 응답 본문은 id만 있으면 충분 — 앱은 로컬 상태를 별도로 갱신하고, 나머지 필드(Gson이
// 모르는 필드)는 무시됨.
data class MealLogOut(val id: Int)
