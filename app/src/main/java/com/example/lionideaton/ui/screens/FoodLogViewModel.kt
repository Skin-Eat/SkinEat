package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lionideaton.data.repository.FoodAnalysisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FoodPhotoAnalysisState {
    data object Idle : FoodPhotoAnalysisState
    data object Loading : FoodPhotoAnalysisState
    data class Success(val candidates: List<String>) : FoodPhotoAnalysisState
    data class Error(val message: String) : FoodPhotoAnalysisState
}

class FoodLogViewModel(
    private val repository: FoodAnalysisRepository = FoodAnalysisRepository()
) : ViewModel() {

    private val _photoAnalysisState = MutableStateFlow<FoodPhotoAnalysisState>(FoodPhotoAnalysisState.Idle)
    val photoAnalysisState: StateFlow<FoodPhotoAnalysisState> = _photoAnalysisState.asStateFlow()

    private var lastCapturedPhoto: Bitmap? = null

    fun analyzeFoodPhoto(bitmap: Bitmap) {
        lastCapturedPhoto = bitmap
        _photoAnalysisState.value = FoodPhotoAnalysisState.Loading
        viewModelScope.launch {
            repository.analyzeFoodPhoto(bitmap)
                .onSuccess { candidates ->
                    _photoAnalysisState.value = if (candidates.isEmpty()) {
                        // 후보가 0개면 API 호출 자체는 성공이지만 사용자 입장에선 이름도 안 뜨고
                        // 선택할 칩(=저장 액션)도 없는 막다른 상태라, 아래 메뉴 검색으로 안내한다.
                        FoodPhotoAnalysisState.Error("이 음식을 인식하지 못했어요. 아래 메뉴 검색으로 직접 등록해주세요.")
                    } else {
                        FoodPhotoAnalysisState.Success(candidates)
                    }
                }
                .onFailure { error ->
                    _photoAnalysisState.value = FoodPhotoAnalysisState.Error(
                        error.message ?: "서버에 연결할 수 없어요. 백엔드 주소가 설정됐는지 확인해주세요."
                    )
                }
        }
    }

    fun retryAnalysis() {
        lastCapturedPhoto?.let { analyzeFoodPhoto(it) }
    }

    fun resetPhotoAnalysis() {
        lastCapturedPhoto = null
        _photoAnalysisState.value = FoodPhotoAnalysisState.Idle
    }
}
