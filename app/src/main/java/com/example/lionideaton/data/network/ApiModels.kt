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

data class MealFoodOut(val id: Int, val name: String)

data class MealItemDetailOut(val id: Int, val portionRatio: Double, val isAiDetected: Boolean, val food: MealFoodOut)

// POST /meals 응답은 id만 쓰지만, GET /meals 히스토리 조회에도 같은 타입을 재사용한다 —
// Gson은 클래스에 없는 필드를 그냥 무시하니 하나로 충분하다.
data class MealLogOut(
    val id: Int,
    val eatenAt: String? = null,
    val mealType: String? = null,
    val photoUrl: String? = null,
    val items: List<MealItemDetailOut> = emptyList()
)
