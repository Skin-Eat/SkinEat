package com.example.lionideaton.data.model

enum class PriceBand { LOW, MID, HIGH }

// Mirrors the `ingredient` table. One key_nutrient has exactly one isPrimary=true row and
// several sub/alternate rows (isPrimary=false) used as substitutes when the user dislikes
// the primary pick. appealNote is hand-written copy, not assembled from a template.
data class Ingredient(
    val id: Long,
    val name: String,
    val keyNutrient: String,
    val purposeTag: String,
    val searchKeyword: String,
    val isPrimary: Boolean,
    val priceBand: PriceBand? = null,
    val appealNote: String? = null
)
