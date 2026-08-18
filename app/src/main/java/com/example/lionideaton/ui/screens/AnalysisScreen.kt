package com.example.lionideaton.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.IngredientSeedData
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CautionTagBackground
import com.example.lionideaton.ui.theme.CautionTagText
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PeachSecondary
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private enum class SkinImpactLevel(val backgroundColor: Color, val textColor: Color) {
    Good(PositiveTagBackground, PositiveTagText),
    Moderate(CautionTagBackground, CautionTagText),
    Bad(WarningTagBackground, WarningTagText)
}

private enum class AnalysisTier(val label: String, val days: Int, val isPremium: Boolean) {
    DAILY("오늘", 0, false),
    WEEKLY("7일", 7, true),
    MONTHLY("30일", 30, true)
}

private data class ConcernIngredient(val label: String, val level: SkinImpactLevel)

private data class MenuSkinContribution(
    val name: String,
    val score: Int,
    val level: SkinImpactLevel,
    val statusLabel: String,
    val description: String
)

private fun levelForScore(score: Int): SkinImpactLevel = when {
    score >= 80 -> SkinImpactLevel.Good
    score >= 60 -> SkinImpactLevel.Moderate
    else -> SkinImpactLevel.Bad
}

private fun statusLabelForScore(score: Int): String = when {
    score >= 80 -> "좋음"
    score >= 60 -> "보통"
    else -> "나쁨"
}

private fun descriptionFor(item: MealItem): String {
    val food = item.food
    return when {
        food.omega3Mg != null && food.omega3Mg * item.portionRatio >= 500.0 -> "오메가3 풍부, 피부 장벽 강화 효과"
        food.satFatG != null && food.satFatG * item.portionRatio >= 5.0 -> "포화지방 다소 높음, 염증성 피부 트러블 유발 가능"
        food.sugarG * item.portionRatio >= 15.0 -> "당류 다소 높음, 콜라겐 손상 유발 가능"
        food.vitCMg != null && food.vitCMg * item.portionRatio >= 30.0 -> "비타민C 풍부, 항산화 효과"
        else -> "특별한 주의 성분 없이 무난한 식사예요"
    }
}

private data class SkinConcernExplanation(val nutrient: String, val explanation: String)

private val skinConcernExplanations = listOf(
    SkinConcernExplanation(
        "나트륨",
        "나트륨을 과다 섭취하면 체내 수분이 세포 밖으로 빠져나가며 피부가 건조해지고 붓기가 잘 생겨요."
    ),
    SkinConcernExplanation(
        "당류",
        "당류(Sugar)는 체내 단백질과 결합해 '최종당화산물(AGEs)'을 만들고, 이는 콜라겐과 엘라스틴 섬유를 파괴해 피부 탄력을 줄여요."
    ),
    SkinConcernExplanation(
        "포화지방",
        "포화지방을 과다 섭취하면 피지 분비가 늘고 염증 반응이 촉진되어 트러블이 잘 생기는 피부 환경이 만들어져요."
    )
)

// General, well-established nutrition-science background — never a stand-in for a real citation.
private val researchInsights = listOf(
    "비타민C는 콜라겐 합성 과정의 필수 보조인자로 작용한다고 널리 알려져 있어요.",
    "오메가-3 지방산은 항염 작용을 도와 피부 장벽 안정에 도움을 줄 수 있다고 알려져 있어요.",
    "당류를 과다 섭취하면 '최종당화산물(AGEs)' 생성이 늘어 콜라겐 섬유의 탄력이 저하될 수 있다는 연구들이 있어요."
)

@Composable
fun AnalysisScreen(modifier: Modifier = Modifier, mealLogViewModel: MealLogViewModel = viewModel()) {
    var tier by remember { mutableStateOf(AnalysisTier.DAILY) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "먹은 만큼 예뻐지는 맞춤 분석",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (tier) {
                    AnalysisTier.DAILY -> "오늘 섭취한 음식이 내일의 피부에 주는 영향이에요."
                    AnalysisTier.WEEKLY -> "최근 7일간의 식습관과 피부 변화를 종합 분석했어요."
                    AnalysisTier.MONTHLY -> "최근 30일간의 식습관과 피부 변화를 종합 분석했어요."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        TierSelector(tier = tier, onTierChange = { tier = it })

        if (tier.isPremium) {
            PremiumBanner()
        }

        when (tier) {
            AnalysisTier.DAILY -> DailyAnalysisContent(mealLogViewModel)
            AnalysisTier.WEEKLY, AnalysisTier.MONTHLY -> PeriodAnalysisContent(days = tier.days, mealLogViewModel = mealLogViewModel)
        }
    }
}

@Composable
private fun TierSelector(tier: AnalysisTier, onTierChange: (AnalysisTier) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceMuted)
            .padding(4.dp)
    ) {
        AnalysisTier.entries.forEach { t ->
            val selected = t == tier
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) CardWhite else Color.Transparent)
                    .clickable { onTierChange(t) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) CoralPrimary else TextSecondary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                if (t.isPremium) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRO",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) CoralPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// No paywall lock yet (explicitly deferred by the team) — this just signals it's the premium tier.
@Composable
private fun PremiumBanner() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = PeachSecondary) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⭐", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "유료 회원 전용 누적 리포트예요. 지금은 잠금 없이 미리 체험할 수 있어요.",
                style = MaterialTheme.typography.labelSmall,
                color = CoralPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DailyAnalysisContent(mealLogViewModel: MealLogViewModel) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val todayEntries = remember(allEntries) { mealLogViewModel.todayEntries() }
    val recentEntries = remember(allEntries) { mealLogViewModel.recentEntries() }
    val todayItems = todayEntries.flatMap { it.items }

    val sugarConcern = todayItems.any { it.food.sugarG * it.portionRatio >= 15.0 }
    val satFatConcern = todayItems.any { item -> item.food.satFatG?.let { it * item.portionRatio >= 5.0 } == true }

    val concernIngredients = listOf(
        ConcernIngredient(if (sugarConcern) "당류 (주의)" else "당류 (안전)", if (sugarConcern) SkinImpactLevel.Moderate else SkinImpactLevel.Good),
        ConcernIngredient(if (satFatConcern) "포화지방 (주의)" else "포화지방 (안전)", if (satFatConcern) SkinImpactLevel.Bad else SkinImpactLevel.Good)
    )

    val concernDescription = when {
        satFatConcern -> "오늘 섭취한 고포화지방 음식으로 인해 피지 분비가 늘고 트러블이 생기기 쉬워질 수 있습니다."
        sugarConcern -> "오늘 섭취한 당류가 다소 높아 최종당화산물(AGEs) 생성으로 피부 탄력이 줄어들 수 있습니다."
        todayItems.isEmpty() -> "아직 오늘 기록한 식사가 없어요. 음식을 기록하면 분석이 시작돼요."
        else -> "오늘 식단에서 특별히 주의할 성분은 발견되지 않았어요."
    }

    val menuContributions = todayEntries.sortedByDescending { it.eatenAt }.flatMap { entry ->
        entry.items.map { item ->
            val score = SkinScoreCalculator.scoreForItem(item)
            MenuSkinContribution(
                name = item.food.name,
                score = score,
                level = levelForScore(score),
                statusLabel = statusLabelForScore(score),
                description = descriptionFor(item)
            )
        }
    }

    val deficiency = SkinScoreCalculator.analyzeDeficiency(recentEntries)

    ConcernIngredientCard(ingredients = concernIngredients, description = concernDescription)

    if (menuContributions.isNotEmpty()) {
        Column {
            Text(
                text = "메뉴별 피부 기여도",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                menuContributions.forEach { MenuContributionCard(it) }
            }
        }
    }

    DeficientNutrientCard(deficiency = deficiency)

    WhyThisResultCard(title = "왜 이런 결과가 나왔나요?", explanations = skinConcernExplanations)
}

@Composable
private fun PeriodAnalysisContent(days: Int, mealLogViewModel: MealLogViewModel) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val periodEntries = remember(allEntries, days) { mealLogViewModel.recentEntries(days.toLong()) }
    val previousPeriodEntries = remember(allEntries, days) {
        val now = LocalDateTime.now()
        val previousStart = now.minusDays((days * 2).toLong())
        val previousEnd = now.minusDays(days.toLong())
        allEntries.filter { it.eatenAt.isAfter(previousStart) && it.eatenAt.isBefore(previousEnd) }
    }

    val loggedDays = remember(periodEntries) { periodEntries.map { it.eatenAt.toLocalDate() }.distinct().size }
    val averageScore = remember(periodEntries) { if (periodEntries.isEmpty()) 0 else SkinScoreCalculator.scoreForEntries(periodEntries) }
    val previousAverageScore = remember(previousPeriodEntries) {
        if (previousPeriodEntries.isEmpty()) null else SkinScoreCalculator.scoreForEntries(previousPeriodEntries)
    }
    val deficiency = remember(periodEntries) { SkinScoreCalculator.analyzeDeficiency(periodEntries) }

    val dailyScores = remember(allEntries, days) {
        (days - 1 downTo 0).map { daysAgo ->
            SkinScoreCalculator.scoreForEntries(mealLogViewModel.entriesForDate(LocalDate.now().minusDays(daysAgo.toLong())))
        }
    }
    val chartValues = remember(dailyScores) {
        if (dailyScores.size > 10) bucketAverage(dailyScores, 6) else dailyScores.map { it.toFloat() }
    }

    val patterns = remember(periodEntries, deficiency) { detectPatterns(periodEntries, deficiency) }
    val improvements = remember(deficiency) { topImprovements(deficiency) }
    val recommendedIngredients = remember(deficiency) { recommendedIngredientsFor(deficiency) }

    PeriodSummaryCard(days = days, loggedDays = loggedDays, averageScore = averageScore, totalMeals = periodEntries.size)

    Column {
        Text(
            text = "지속적으로 부족한 영양소 발견",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        DeficientNutrientCard(deficiency = deficiency, periodLabel = "최근 ${days}일간")
    }

    Column {
        Text(
            text = "점수 변화 추이",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        PeriodTrendCard(values = chartValues, days = days)
    }

    Column {
        Text(
            text = "반복되는 식습관 패턴 발견",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        BulletCard(items = patterns)
    }

    Column {
        Text(
            text = "개인화된 우선 개선사항 Top 3",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        NumberedCard(items = improvements)
    }

    Column {
        Text(
            text = "기간 전체를 고려한 Skin Basket",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recommendedIngredients.forEach { ingredient ->
                IngredientMiniCard(ingredient = ingredient, modifier = Modifier.weight(1f))
            }
        }
    }

    Column {
        Text(
            text = "지난 목표 달성 여부 및 변화 추적",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        GoalTrackingCard(currentAverage = averageScore, previousAverage = previousAverageScore, days = days)
    }

    WhyThisResultCard(title = "논문 근거 기반 상세 리포트", explanations = null, insights = researchInsights)
}

private fun bucketAverage(values: List<Int>, bucketCount: Int): List<Float> {
    if (values.isEmpty()) return emptyList()
    val chunkSize = (values.size + bucketCount - 1) / bucketCount
    return values.chunked(chunkSize).map { chunk -> chunk.map { it.toFloat() }.average().toFloat() }
}

private fun detectPatterns(entries: List<MealLogEntry>, deficiency: SkinScoreCalculator.DeficiencySummary): List<String> {
    val patterns = mutableListOf<String>()
    val nameCounts = entries.flatMap { it.items }.groupingBy { it.food.name }.eachCount()
    nameCounts.filter { it.value >= 2 }.entries.sortedByDescending { it.value }.take(2).forEach { (name, count) ->
        patterns.add("'${name}'을(를) 이 기간 동안 ${count}번 드셨어요.")
    }
    val lateNightCount = entries.count { it.eatenAt.toLocalTime().isAfter(LocalTime.of(21, 0)) }
    if (lateNightCount >= 2) {
        patterns.add("밤 9시 이후 식사가 ${lateNightCount}번 있었어요.")
    }
    if (deficiency.highSugarMealCount >= 3) {
        patterns.add("당류가 높은 식사가 ${deficiency.highSugarMealCount}번 반복됐어요.")
    }
    if (deficiency.highSatFatMealCount >= 3) {
        patterns.add("포화지방이 높은 식사가 ${deficiency.highSatFatMealCount}번 반복됐어요.")
    }
    return patterns.ifEmpty { listOf("뚜렷하게 반복되는 식습관 패턴은 아직 발견되지 않았어요.") }
}

private fun topImprovements(deficiency: SkinScoreCalculator.DeficiencySummary): List<String> {
    val candidates = mutableListOf<Pair<Int, String>>()
    if (deficiency.highSugarMealCount >= 2) candidates.add(4 to "당류가 높은 음식 섭취 빈도를 줄여보세요.")
    if (deficiency.highSatFatMealCount >= 2) candidates.add(4 to "포화지방이 높은 음식 섭취 빈도를 줄여보세요.")
    if (deficiency.omega3Deficient) candidates.add(3 to "오메가3가 풍부한 연어·고등어를 주 2회 이상 추가해보세요.")
    if (deficiency.vitCDeficient) candidates.add(2 to "비타민C가 풍부한 브로콜리·파프리카를 매 끼니에 곁들여보세요.")
    if (deficiency.vitEDeficient) candidates.add(2 to "비타민E가 풍부한 아몬드·아보카도를 간식으로 챙겨보세요.")
    if (deficiency.zincDeficient) candidates.add(2 to "아연이 풍부한 굴·달걀을 식단에 추가해보세요.")

    val fallback = listOf(
        "지금의 균형 잡힌 식습관을 계속 유지해보세요.",
        "채소와 수분 섭취를 꾸준히 챙겨보세요.",
        "다양한 색깔의 식재료를 골고루 섭취해보세요."
    )
    val ranked = candidates.sortedByDescending { it.first }.map { it.second }.distinct()
    return (ranked + fallback).take(3)
}

private fun recommendedIngredientsFor(deficiency: SkinScoreCalculator.DeficiencySummary): List<Ingredient> {
    val deficientKeys = buildList {
        if (deficiency.omega3Deficient) add(IngredientSeedData.KEY_OMEGA3)
        if (deficiency.vitCDeficient) add(IngredientSeedData.KEY_VIT_C)
        if (deficiency.vitEDeficient) add(IngredientSeedData.KEY_VIT_E)
        if (deficiency.zincDeficient) add(IngredientSeedData.KEY_ZINC)
    }
    val keys = deficientKeys.ifEmpty {
        listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E, IngredientSeedData.KEY_ZINC)
    }
    return keys.mapNotNull { IngredientSeedData.primaryFor(it) }
}

@Composable
private fun PeriodSummaryCard(days: Int, loggedDays: Int, averageScore: Int, totalMeals: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "최근 ${days}일 식생활 종합 분석",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat(label = "기록일", value = "${loggedDays}/${days}일")
                SummaryStat(label = "총 기록", value = "${totalMeals}끼")
                SummaryStat(label = "평균 점수", value = "${averageScore}점")
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun PeriodTrendCard(values: List<Float>, days: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (values.size >= 2) {
                TrendLineChart(values = values, modifier = Modifier.fillMaxWidth().height(120.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${days}일 전", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = "오늘", style = MaterialTheme.typography.labelSmall, color = CoralPrimary, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = "추이를 보여줄 만큼 기록이 아직 충분하지 않아요.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun TrendLineChart(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minVal = values.min()
        val maxVal = values.max()
        val range = (maxVal - minVal).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val verticalPadding = 8.dp.toPx()
        val points = values.mapIndexed { index, value ->
            val normalized = (value - minVal) / range
            Offset(
                x = index * stepX,
                y = size.height - verticalPadding - normalized * (size.height - 2 * verticalPadding)
            )
        }

        drawLine(
            color = SurfaceMuted,
            start = Offset(0f, size.height - verticalPadding),
            end = Offset(size.width, size.height - verticalPadding),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = path,
            color = CoralPrimary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        points.forEach { point -> drawCircle(color = CoralPrimary, radius = 4.dp.toPx(), center = point) }
    }
}

@Composable
private fun BulletCard(items: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { text ->
                Row {
                    Text(text = "· ", style = MaterialTheme.typography.bodyMedium, color = CoralPrimary, fontWeight = FontWeight.Bold)
                    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun NumberedCard(items: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEachIndexed { index, text ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50), color = PeachSecondary, modifier = Modifier.padding(end = 10.dp)) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun IngredientMiniCard(ingredient: Ingredient, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ingredient.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = ingredient.purposeTag, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun GoalTrackingCard(currentAverage: Int, previousAverage: Int?, days: Int) {
    val targetScore = 75
    val achieved = currentAverage >= targetScore
    val badgeBackground = if (achieved) PositiveTagBackground else WarningTagBackground
    val badgeText = if (achieved) PositiveTagText else WarningTagText

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = badgeBackground) {
                    Text(
                        text = if (achieved) "목표 달성" else "목표 미달성",
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "평균 ${currentAverage}점 / 목표 ${targetScore}점", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (previousAverage != null) {
                    val diff = currentAverage - previousAverage
                    val sign = if (diff > 0) "+" else ""
                    "이전 ${days}일 대비 ${sign}${diff}점 변화했어요."
                } else {
                    "이전 기간과 비교할 기록이 아직 부족해요."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "* 목표 점수는 서비스 기본값이며, 추후 직접 설정 기능이 추가될 예정이에요.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ConcernIngredientCard(ingredients: List<ConcernIngredient>, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = WarningTagText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "피부 트러블 주의 성분",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEach { ingredient ->
                    LevelTag(text = ingredient.label, level = ingredient.level)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "나트륨 데이터는 아직 준비되지 않아 이 분석에는 포함되지 않았어요.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LevelTag(text: String, level: SkinImpactLevel) {
    Surface(shape = RoundedCornerShape(50), color = level.backgroundColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = level.textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MenuContributionCard(item: MenuSkinContribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.score}점",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = item.level.textColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LevelTag(text = item.statusLabel, level = item.level)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun DeficientNutrientCard(deficiency: SkinScoreCalculator.DeficiencySummary, periodLabel: String = "최근 7일간") {
    val deficientNames = buildList {
        if (deficiency.omega3Deficient) add("오메가3")
        if (deficiency.vitCDeficient) add("비타민C")
        if (deficiency.vitEDeficient) add("비타민E")
        if (deficiency.zincDeficient) add("아연")
    }
    val hasDeficiency = deficientNames.isNotEmpty()
    val backgroundColor = if (hasDeficiency) WarningTagBackground else PositiveTagBackground
    val titleColor = if (hasDeficiency) WarningTagText else PositiveTagText
    val message = if (hasDeficiency) {
        "${periodLabel} ${deficientNames.joinToString(", ")} 섭취가 권장 수준보다 부족해요. 콜라겐 합성과 장벽 복구를 위해 이 성분이 담긴 식재료를 추천해요!"
    } else {
        "${periodLabel} 주요 피부 영양소를 고르게 섭취하고 있어요. 지금 흐름을 유지해보세요!"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "부족한 피부 유효 성분",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
        }
    }
}

@Composable
private fun WhyThisResultCard(title: String, explanations: List<SkinConcernExplanation>?, insights: List<String>? = null) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "접기" else "펼치기",
                    tint = TextSecondary
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                if (explanations != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        explanations.forEach { item ->
                            Column {
                                Text(
                                    text = item.nutrient,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else if (insights != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        insights.forEach { text ->
                            Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "특정 논문을 인용한 것이 아닌, 널리 알려진 일반 영양학적 지식을 바탕으로 구성됐어요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
