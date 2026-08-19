package com.example.lionideaton.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.PriceBand

// Grouped by the 4 nutrient axes the `food` table actually supports (no lycopene/sodium
// columns exist yet, so those aren't modeled). One primary + a couple of subs per axis,
// matching the backend's ingredient table sample rows where given.
object IngredientSeedData {
    const val KEY_OMEGA3 = "OMEGA3"
    const val KEY_VIT_C = "VIT_C"
    const val KEY_VIT_E = "VIT_E"
    const val KEY_ZINC = "ZINC"

    val ingredients: List<Ingredient> = listOf(
        Ingredient(1, "연어", KEY_OMEGA3, "오메가3 보충", "생연어 필렛", isPrimary = true, priceBand = PriceBand.HIGH),
        Ingredient(2, "고등어", KEY_OMEGA3, "오메가3 보충", "국내산 고등어", isPrimary = false, priceBand = PriceBand.LOW, appealNote = "가격 부담이 낮아요"),
        Ingredient(3, "들기름", KEY_OMEGA3, "오메가3 보충", "들기름", isPrimary = false, priceBand = PriceBand.MID, appealNote = "조리 없이 간편해요"),

        Ingredient(4, "브로콜리", KEY_VIT_C, "비타민C 보충", "유기농 브로콜리", isPrimary = true, priceBand = PriceBand.LOW),
        Ingredient(5, "파프리카", KEY_VIT_C, "비타민C 보충", "파프리카", isPrimary = false, priceBand = PriceBand.LOW, appealNote = "생으로 먹기 편해요"),
        Ingredient(6, "키위", KEY_VIT_C, "비타민C 보충", "키위", isPrimary = false, priceBand = PriceBand.LOW, appealNote = "간식으로 먹기 좋아요"),

        Ingredient(7, "아몬드", KEY_VIT_E, "비타민E 보충", "아몬드", isPrimary = true, priceBand = PriceBand.MID),
        Ingredient(8, "아보카도", KEY_VIT_E, "비타민E 보충", "아보카도", isPrimary = false, priceBand = PriceBand.MID, appealNote = "샐러드에 곁들이기 좋아요"),
        Ingredient(9, "해바라기씨", KEY_VIT_E, "비타민E 보충", "해바라기씨", isPrimary = false, priceBand = PriceBand.LOW, appealNote = "간편하게 먹기 좋아요"),

        Ingredient(10, "굴", KEY_ZINC, "아연 보충", "생굴", isPrimary = true, priceBand = PriceBand.HIGH),
        Ingredient(11, "달걀", KEY_ZINC, "아연 보충", "달걀", isPrimary = false, priceBand = PriceBand.LOW, appealNote = "가장 쉽게 구할 수 있어요"),
        Ingredient(12, "견과류", KEY_ZINC, "아연 보충", "혼합 견과류", isPrimary = false, priceBand = PriceBand.MID)
    )

    fun primaryFor(keyNutrient: String): Ingredient? = ingredients.firstOrNull { it.keyNutrient == keyNutrient && it.isPrimary }

    fun subsFor(keyNutrient: String, excludeNames: Set<String> = emptySet()): List<Ingredient> =
        ingredients.filter { it.keyNutrient == keyNutrient && !it.isPrimary && it.name !in excludeNames }

    fun iconFor(keyNutrient: String): ImageVector = when (keyNutrient) {
        KEY_OMEGA3, KEY_ZINC -> Icons.Filled.SetMeal
        else -> Icons.Filled.Eco
    }

    // Placeholder pricing (KRW) derived from price_band until this is sourced from a real product API.
    fun estimatedPrice(priceBand: PriceBand?): Int = when (priceBand) {
        PriceBand.LOW -> 3000
        PriceBand.MID -> 6000
        PriceBand.HIGH -> 12000
        null -> 5000
    }
}
