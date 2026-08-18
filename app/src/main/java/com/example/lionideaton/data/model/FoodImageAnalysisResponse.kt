package com.example.lionideaton.data.model

// Matches the draft spec's POST /ai/food-image response shape: { "candidates": ["마라탕", "짬뽕"] }
data class FoodImageAnalysisResponse(
    val candidates: List<String> = emptyList()
)
