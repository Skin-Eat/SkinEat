package com.example.lionideaton.domain

import com.example.lionideaton.data.model.Food
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.data.model.MealLogEntry
import kotlin.math.roundToInt

/**
 * Rule-based (non-ML) Skin Score engine, matching the backend DB schema's four score axes:
 * 글당부하(glycemic load) via sugar/high-GI, 항염(anti-inflammatory) via sat-fat/omega3,
 * 장벽·수분(barrier/moisture) via protein/zinc, 항산화(antioxidant) via vitA/vitC/vitE.
 * There is no sodium column in the `food` table yet (still undecided per the backend doc),
 * so sodium is intentionally NOT part of this calculation.
 *
 * Nullable nutrients mean "no data" and are skipped entirely (never treated as 0), per the
 * backend team's explicit note: a null omega3 must not drag a food's score down as if it had none.
 */
object SkinScoreCalculator {

    private const val BASE_SCORE = 70

    private const val HIGH_SUGAR_G = 15.0
    private const val HIGH_SAT_FAT_G = 5.0
    private const val GOOD_OMEGA3_MG = 500.0
    private const val GOOD_VIT_C_MG = 30.0
    private const val GOOD_VIT_E_MG = 3.0
    private const val GOOD_ZINC_MG = 2.0

    fun scoreForItem(item: MealItem): Int {
        val food = item.food
        val ratio = item.portionRatio
        var score = BASE_SCORE

        if (food.sugarG * ratio >= HIGH_SUGAR_G) score -= 10
        if (food.isHighGi) score -= 5
        food.satFatG?.let { if (it * ratio >= HIGH_SAT_FAT_G) score -= 5 }
        food.omega3Mg?.let { if (it * ratio >= GOOD_OMEGA3_MG) score += 10 }
        food.vitCMg?.let { if (it * ratio >= GOOD_VIT_C_MG) score += 5 }
        food.vitEMg?.let { if (it * ratio >= GOOD_VIT_E_MG) score += 5 }
        food.zincMg?.let { if (it * ratio >= GOOD_ZINC_MG) score += 5 }

        return score.coerceIn(0, 100)
    }

    fun scoreForMeals(items: List<MealItem>): Int {
        if (items.isEmpty()) return BASE_SCORE
        return items.map { scoreForItem(it) }.average().roundToInt().coerceIn(0, 100)
    }

    fun scoreForEntries(entries: List<MealLogEntry>): Int = scoreForMeals(entries.flatMap { it.items })

    data class NutrientAverage(val average: Double?, val sampleCount: Int)

    // Public so screens can pull their own derived stats (e.g. an "antioxidant index" for display)
    // without duplicating the null-exclusion averaging rule.
    fun averageNutrient(entries: List<MealLogEntry>, selector: (Food) -> Double?): NutrientAverage {
        val values = entries.flatMap { entry ->
            entry.items.mapNotNull { item -> selector(item.food)?.times(item.portionRatio) }
        }
        return if (values.isEmpty()) NutrientAverage(null, 0) else NutrientAverage(values.average(), values.size)
    }

    data class DeficiencySummary(
        val omega3: NutrientAverage,
        val vitC: NutrientAverage,
        val vitE: NutrientAverage,
        val zinc: NutrientAverage,
        val omega3Deficient: Boolean,
        val vitCDeficient: Boolean,
        val vitEDeficient: Boolean,
        val zincDeficient: Boolean,
        val highSugarMealCount: Int,
        val highSatFatMealCount: Int
    )

    // Analyzes a rolling window of recent meal logs (e.g. the last 7 days) for deficiencies —
    // computed live from the entries passed in, never cached, matching the backend's "저장하지
    // 않는 것" design (7-day rollups are cheap enough to recompute on every call).
    fun analyzeDeficiency(entries: List<MealLogEntry>): DeficiencySummary {
        val omega3 = averageNutrient(entries) { it.omega3Mg }
        val vitC = averageNutrient(entries) { it.vitCMg }
        val vitE = averageNutrient(entries) { it.vitEMg }
        val zinc = averageNutrient(entries) { it.zincMg }

        val allItems = entries.flatMap { it.items }
        val highSugarCount = allItems.count { it.food.sugarG * it.portionRatio >= HIGH_SUGAR_G }
        val highSatFatCount = allItems.count { item ->
            item.food.satFatG?.let { it * item.portionRatio >= HIGH_SAT_FAT_G } == true
        }

        return DeficiencySummary(
            omega3 = omega3,
            vitC = vitC,
            vitE = vitE,
            zinc = zinc,
            omega3Deficient = (omega3.average ?: Double.MAX_VALUE) < GOOD_OMEGA3_MG,
            vitCDeficient = (vitC.average ?: Double.MAX_VALUE) < GOOD_VIT_C_MG,
            vitEDeficient = (vitE.average ?: Double.MAX_VALUE) < GOOD_VIT_E_MG,
            zincDeficient = (zinc.average ?: Double.MAX_VALUE) < GOOD_ZINC_MG,
            highSugarMealCount = highSugarCount,
            highSatFatMealCount = highSatFatCount
        )
    }
}
