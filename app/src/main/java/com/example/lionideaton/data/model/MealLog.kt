package com.example.lionideaton.data.model

import java.time.LocalDateTime

enum class MealType(val label: String) {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식")
}

// Mirrors `meal_item`: one food within a meal, portionRatio 0.5 = half a serving.
data class MealItem(
    val food: Food,
    val portionRatio: Double = 1.0
)

// Mirrors `meal_log` + its `meal_item` rows joined together. No score column here —
// scores are always computed on the fly (see SkinScoreCalculator), matching the backend design.
data class MealLogEntry(
    val id: Long,
    val eatenAt: LocalDateTime,
    val mealType: MealType,
    val items: List<MealItem>
)
