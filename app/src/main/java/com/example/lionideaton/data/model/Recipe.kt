package com.example.lionideaton.data.model

// A skin benefit tied to one real nutrient in the recipe's ingredients — phrased as a general,
// hedged nutrition-education fact ("도움을 줄 수 있어요"), never as a guaranteed personal outcome
// or a fabricated statistic. Mirrors `recipe` table's generated_by = curated for now (no LLM wired).
data class SkinBenefit(val nutrient: String, val description: String)

data class RecipeDetail(
    val name: String,
    val cookingTimeMinutes: Int,
    val servings: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val skinBenefits: List<SkinBenefit>
)
