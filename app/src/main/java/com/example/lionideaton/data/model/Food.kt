package com.example.lionideaton.data.model

enum class FoodSource { MFDS, USER_ADDED }

// Mirrors the backend `food` table. The six nutrient fields below `fatG` are nullable because
// the source (MFDS) data is often incomplete — a null means "no data", NOT zero, and must be
// excluded from averages rather than counted as a deficiency.
data class Food(
    val id: Long,
    val name: String,
    val source: FoodSource,
    val servingG: Int,
    val energyKcal: Double,
    val carbG: Double,
    val sugarG: Double,
    val proteinG: Double,
    val fatG: Double,
    val satFatG: Double? = null,
    val omega3Mg: Double? = null,
    val vitAUg: Double? = null,
    val vitCMg: Double? = null,
    val vitEMg: Double? = null,
    val zincMg: Double? = null,
    val isDairy: Boolean = false,
    val isHighGi: Boolean = false
)
