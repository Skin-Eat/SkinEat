package com.example.lionideaton.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lionideaton.data.local.SessionStore
import com.example.lionideaton.data.model.ConstraintType
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.data.model.SkinType
import com.example.lionideaton.data.model.UserConstraintItem
import com.example.lionideaton.data.model.UserProfile
import com.example.lionideaton.data.network.ConstraintRequest
import com.example.lionideaton.data.network.LoginRequest
import com.example.lionideaton.data.network.NetworkModule
import com.example.lionideaton.data.network.SignupRequest
import com.example.lionideaton.data.network.UpdateMeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

// AuthScreen이 로딩/에러를 보여줄 수 있게 하는 상태. Idle -> Loading -> (Success | Error).
// Success/Error를 본 화면은 resetAuthState()를 불러서 다시 Idle로 돌려놔야 다음 시도 때
// 이전 결과가 재사용되지 않는다.
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

// Activity-scoped shared user profile — Auth(로그인/가입), Onboarding, Home/My, Basket이 공유.
// 백엔드 signup/login에서 받은 accessToken은 NetworkModule에 저장되고, 이후 모든 API 호출에
// 자동으로 Authorization 헤더로 붙는다(NetworkModule.authToken 참고). 같은 토큰을 SessionStore
// (DataStore)에도 저장해서 앱을 완전히 껐다 켜도 로그인이 유지되게 한다.
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionStore = SessionStore(application)

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // 저장된 세션을 복원하는 동안 true — MainActivity가 이 시간 동안 로그인 화면을 잠깐
    // 보여줬다 지우는 깜빡임 없이 로딩 상태를 보여줄 수 있게.
    private val _sessionRestoring = MutableStateFlow(true)
    val sessionRestoring: StateFlow<Boolean> = _sessionRestoring.asStateFlow()

    init {
        viewModelScope.launch {
            restoreSession()
            _sessionRestoring.value = false
        }
    }

    private suspend fun restoreSession() {
        val token = sessionStore.tokenFlow.first() ?: return
        NetworkModule.authToken = token
        try {
            val me = NetworkModule.api.getMe().data ?: run {
                NetworkModule.authToken = null
                sessionStore.clearToken()
                return
            }
            val constraints = NetworkModule.api.getConstraints().data.orEmpty()
            _profile.value = UserProfile(
                nickname = me.nickname,
                skinType = me.skinType?.let { raw -> runCatching { SkinType.valueOf(raw) }.getOrNull() },
                concerns = me.concerns.mapNotNull { raw -> runCatching { SkinConcern.valueOf(raw) }.getOrNull() },
                constraints = constraints.mapNotNull { c ->
                    runCatching { UserConstraintItem(ConstraintType.valueOf(c.type.uppercase()), c.ingredientName) }.getOrNull()
                }
            )
            _onboardingCompleted.value = me.skinType != null
            _isLoggedIn.value = true
        } catch (e: Exception) {
            // 저장된 토큰이 만료/무효한 경우 — 로그아웃 상태로 남겨두고 재로그인을 유도한다.
            NetworkModule.authToken = null
            sessionStore.clearToken()
        }
    }

    fun resetAuthState() {
        _authState.value = AuthUiState.Idle
    }

    fun logout() {
        NetworkModule.authToken = null
        _isLoggedIn.value = false
        _onboardingCompleted.value = false
        _profile.value = UserProfile()
        viewModelScope.launch { sessionStore.clearToken() }
    }

    // HttpException(4xx/5xx)엔 우리 envelope의 {error:{message}}가 body로 들어있는데, 기본
    // e.message는 "HTTP 400 Bad Request"처럼 원인을 알 수 없는 문자열이라 이걸 직접 꺼낸다.
    private fun errorMessageFrom(e: Exception): String {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string()
            val serverMessage = body?.let {
                runCatching { JSONObject(it).optJSONObject("error")?.optString("message") }.getOrNull()
            }
            if (!serverMessage.isNullOrBlank()) return serverMessage
        }
        return "네트워크 오류: ${e.message ?: "서버에 연결할 수 없어요"}"
    }

    fun signUp(email: String, password: String, nickname: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = NetworkModule.api.signup(SignupRequest(email = email, password = password, nickname = nickname))
                val body = response.data
                if (!response.success || body == null) {
                    _authState.value = AuthUiState.Error(response.error?.message ?: "회원가입에 실패했어요")
                    return@launch
                }
                NetworkModule.authToken = body.accessToken
                sessionStore.saveToken(body.accessToken)
                _profile.value = UserProfile(email = email, nickname = body.user.nickname)
                // 가입 직후엔 온보딩을 아직 안 했으니 skinType이 항상 null로 옴 — 확정 규칙.
                _onboardingCompleted.value = false
                _isLoggedIn.value = true
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(errorMessageFrom(e))
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = NetworkModule.api.login(LoginRequest(email = email, password = password))
                val body = response.data
                if (!response.success || body == null) {
                    _authState.value = AuthUiState.Error(response.error?.message ?: "이메일 또는 비밀번호가 올바르지 않아요")
                    return@launch
                }
                NetworkModule.authToken = body.accessToken
                sessionStore.saveToken(body.accessToken)
                _profile.value = UserProfile(
                    email = email,
                    nickname = body.user.nickname,
                    skinType = body.user.skinType?.let { raw -> runCatching { SkinType.valueOf(raw) }.getOrNull() },
                    concerns = body.user.concerns.mapNotNull { raw -> runCatching { SkinConcern.valueOf(raw) }.getOrNull() }
                )
                // 백엔드엔 "온보딩 완료" 플래그가 따로 없어서, skinType이 채워져 있으면
                // 온보딩을 이미 마친 것으로 취급한다(온보딩에서만 skinType을 채우므로).
                _onboardingCompleted.value = body.user.skinType != null
                _isLoggedIn.value = true
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(errorMessageFrom(e))
            }
        }
    }

    fun completeOnboarding(
        skinType: SkinType,
        concerns: List<SkinConcern>,
        allergies: List<String>,
        dislikes: List<String>
    ) {
        _profile.update {
            it.copy(
                skinType = skinType,
                concerns = concerns,
                constraints = allergies.map { name -> UserConstraintItem(ConstraintType.ALLERGY, name) } +
                    dislikes.map { name -> UserConstraintItem(ConstraintType.DISLIKE, name) }
            )
        }
        _onboardingCompleted.value = true

        viewModelScope.launch {
            try {
                NetworkModule.api.updateMe(
                    UpdateMeRequest(skinType = skinType.name, concerns = concerns.map { it.name })
                )
                allergies.forEach { name ->
                    NetworkModule.api.addConstraint(ConstraintRequest(type = "allergy", ingredientName = name))
                }
                dislikes.forEach { name ->
                    NetworkModule.api.addConstraint(ConstraintRequest(type = "dislike", ingredientName = name))
                }
            } catch (e: Exception) {
                // 로컬 상태는 이미 갱신됐으니 화면은 정상 진행 — 백엔드 반영만 실패한 것.
                // TODO: 재시도/오프라인 큐 없음. 데모 이후 개선 대상.
            }
        }
    }

    fun updateSkinTargets(concerns: List<SkinConcern>, allergies: List<String>, dislikes: List<String>) {
        _profile.update {
            it.copy(
                concerns = concerns,
                constraints = allergies.map { name -> UserConstraintItem(ConstraintType.ALLERGY, name) } +
                    dislikes.map { name -> UserConstraintItem(ConstraintType.DISLIKE, name) }
            )
        }

        viewModelScope.launch {
            try {
                NetworkModule.api.updateMe(UpdateMeRequest(concerns = concerns.map { it.name }))
                allergies.forEach { name -> NetworkModule.api.addConstraint(ConstraintRequest("allergy", name)) }
                dislikes.forEach { name -> NetworkModule.api.addConstraint(ConstraintRequest("dislike", name)) }
            } catch (e: Exception) {
                // 로컬 상태는 이미 갱신됨 — 위 completeOnboarding과 동일한 폴백 원칙.
            }
        }
    }
}
