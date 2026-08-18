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
                .onSuccess { candidates -> _photoAnalysisState.value = FoodPhotoAnalysisState.Success(candidates) }
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
