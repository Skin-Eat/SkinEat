package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.IngredientSeedData
import com.example.lionideaton.data.model.Food
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.data.model.MealType
import com.example.lionideaton.data.model.SkinLogEntry
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
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class ReportStage { SELECT, LOADING, RESULT }
private enum class ReportCalendarType(val label: String) { SKIN("피부달력"), FOOD("음식달력") }
private enum class ReportPeriodPreset { LAST_7, LAST_30, CUSTOM }

@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    skinLogViewModel: SkinLogViewModel = viewModel(),
    mealLogViewModel: MealLogViewModel = viewModel()
) {
    var stage by remember { mutableStateOf(ReportStage.SELECT) }
    var calendarType by remember { mutableStateOf(ReportCalendarType.SKIN) }
    var preset by remember { mutableStateOf(ReportPeriodPreset.LAST_7) }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var customStart by remember { mutableStateOf<LocalDate?>(null) }
    var customEnd by remember { mutableStateOf<LocalDate?>(null) }

    val today = remember { LocalDate.now() }
    val skinEntries by skinLogViewModel.entries.collectAsState()
    val mealEntries by mealLogViewModel.entries.collectAsState()

    val rangeStart: LocalDate
    val rangeEnd: LocalDate
    when (preset) {
        ReportPeriodPreset.LAST_7 -> { rangeStart = today.minusDays(6); rangeEnd = today }
        ReportPeriodPreset.LAST_30 -> { rangeStart = today.minusDays(29); rangeEnd = today }
        ReportPeriodPreset.CUSTOM -> {
            val start = customStart ?: today
            val end = customEnd ?: start
            rangeStart = if (start.isAfter(end)) end else start
            rangeEnd = if (start.isAfter(end)) start else end
        }
    }

    fun onDateTap(date: LocalDate) {
        if (preset != ReportPeriodPreset.CUSTOM || (customStart != null && customEnd != null)) {
            preset = ReportPeriodPreset.CUSTOM
            customStart = date
            customEnd = null
        } else if (customStart == null) {
            customStart = date
        } else {
            customEnd = date
        }
    }

    LaunchedEffect(stage) {
        if (stage == ReportStage.LOADING) {
            delay(1200)
            stage = ReportStage.RESULT
        }
    }

    when (stage) {
        ReportStage.SELECT -> ReportCalendarSelectContent(
            modifier = modifier,
            calendarType = calendarType,
            onCalendarTypeChange = { calendarType = it },
            displayMonth = displayMonth,
            onMonthChange = { displayMonth = it },
            preset = preset,
            onPresetSelected = { selected ->
                preset = selected
                if (selected != ReportPeriodPreset.CUSTOM) {
                    customStart = null
                    customEnd = null
                }
            },
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            onDateTap = ::onDateTap,
            skinEntries = skinEntries,
            mealLogViewModel = mealLogViewModel,
            mealEntriesVersion = mealEntries.size,
            onGenerateClick = { stage = ReportStage.LOADING }
        )

        ReportStage.LOADING -> ReportLoadingContent(modifier = modifier)

        ReportStage.RESULT -> GeneratedReportContent(
            modifier = modifier,
            calendarType = calendarType,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            skinLogViewModel = skinLogViewModel,
            mealLogViewModel = mealLogViewModel,
            onBackToSelect = { stage = ReportStage.SELECT }
        )
    }
}

@Composable
private fun ReportCalendarSelectContent(
    modifier: Modifier = Modifier,
    calendarType: ReportCalendarType,
    onCalendarTypeChange: (ReportCalendarType) -> Unit,
    displayMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    preset: ReportPeriodPreset,
    onPresetSelected: (ReportPeriodPreset) -> Unit,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    onDateTap: (LocalDate) -> Unit,
    skinEntries: List<SkinLogEntry>,
    mealLogViewModel: MealLogViewModel,
    mealEntriesVersion: Int,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(text = "리포트 만들기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "확인하고 싶은 기간과 종류를 선택해주세요", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(SurfaceMuted)
                .padding(4.dp)
        ) {
            ReportCalendarType.entries.forEach { type ->
                val selected = type == calendarType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) CardWhite else Color.Transparent)
                        .clickable { onCalendarTypeChange(type) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) CoralPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onMonthChange(displayMonth.minusMonths(1)) }) {
                        Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "이전 달", tint = TextSecondary)
                    }
                    Text(
                        text = "${displayMonth.year}년 ${displayMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onMonthChange(displayMonth.plusMonths(1)) }) {
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "다음 달", tint = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                val firstOfMonth = displayMonth.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Sunday(7)->0, Monday(1)->1, ...
                val daysInMonth = displayMonth.lengthOfMonth()
                val totalCells = leadingBlanks + daysInMonth
                val rowCount = (totalCells + 6) / 7

                for (row in 0 until rowCount) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - leadingBlanks + 1
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (dayNumber in 1..daysInMonth) {
                                    val date = displayMonth.atDay(dayNumber)
                                    val dotColor = if (calendarType == ReportCalendarType.SKIN) {
                                        skinDotColor(date, skinEntries)
                                    } else {
                                        foodDotColor(date, mealLogViewModel)
                                    }
                                    CalendarDayCell(
                                        date = date,
                                        isToday = date == LocalDate.now(),
                                        inRange = !date.isBefore(rangeStart) && !date.isAfter(rangeEnd),
                                        dotColor = dotColor,
                                        onClick = { onDateTap(date) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodChip(text = "최근 7일", selected = preset == ReportPeriodPreset.LAST_7, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.LAST_7) })
            PeriodChip(text = "최근 30일", selected = preset == ReportPeriodPreset.LAST_30, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.LAST_30) })
            PeriodChip(text = "직접 선택", selected = preset == ReportPeriodPreset.CUSTOM, modifier = Modifier.weight(1f), onClick = { onPresetSelected(ReportPeriodPreset.CUSTOM) })
        }

        if (preset == ReportPeriodPreset.CUSTOM) {
            Text(text = "캘린더에서 시작일과 종료일을 눌러 선택해주세요", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Text(
            text = "선택 기간: ${rangeStart.monthValue}.${rangeStart.dayOfMonth} ~ ${rangeEnd.monthValue}.${rangeEnd.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
        ) {
            Text(text = "리포트 생성하기", fontWeight = FontWeight.Bold)
        }
    }
}

private fun skinDotColor(date: LocalDate, entries: List<SkinLogEntry>): Color? {
    val dayEntries = entries.filter { it.loggedAt.toLocalDate() == date }
    if (dayEntries.isEmpty()) return null
    val average = dayEntries.flatMap { listOf(it.troubleLevel, it.oilLevel, it.drynessLevel) }.average()
    return when {
        average <= 2.0 -> PositiveTagText
        average <= 3.5 -> CautionTagText
        else -> WarningTagText
    }
}

private fun foodDotColor(date: LocalDate, mealLogViewModel: MealLogViewModel): Color? {
    val dayEntries = mealLogViewModel.entriesForDate(date)
    if (dayEntries.isEmpty()) return null
    val score = SkinScoreCalculator.scoreForEntries(dayEntries)
    return when {
        score >= 75 -> PositiveTagText
        score >= 55 -> CautionTagText
        else -> WarningTagText
    }
}

@Composable
private fun CalendarDayCell(date: LocalDate, isToday: Boolean, inRange: Boolean, dotColor: Color?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = if (inRange) CoralPrimary.copy(alpha = 0.15f) else Color.Transparent,
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) CoralPrimary else TextPrimary,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color = dotColor ?: Color.Transparent, shape = CircleShape)
        )
    }
}

@Composable
private fun PeriodChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) CoralPrimary else SurfaceMuted
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun ReportLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = CoralPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "리포트를 생성하고 있어요...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

// General, well-established nutrition-science background — never a stand-in for a real citation.
private val researchInsights = listOf(
    "비타민C는 콜라겐 합성 과정의 필수 보조인자로 작용한다고 널리 알려져 있어요.",
    "오메가-3 지방산은 항염 작용을 도와 피부 장벽 안정에 도움을 줄 수 있다고 알려져 있어요.",
    "당류를 과다 섭취하면 '최종당화산물(AGEs)' 생성이 늘어 콜라겐 섬유의 탄력이 저하될 수 있다는 연구들이 있어요."
)

private data class SkinChangeStat(val label: String, val value: String, val statusLabel: String, val isPositive: Boolean)

private val mockSkinChangeStats = listOf(
    SkinChangeStat("트러블 빈도", "-24%", "감소", isPositive = true),
    SkinChangeStat("유분 지수", "-12%", "조절됨", isPositive = true),
    SkinChangeStat("건조함", "+8%", "건조", isPositive = false)
)

private fun deltaStat(label: String, firstVal: Int, lastVal: Int): SkinChangeStat {
    val diff = lastVal - firstVal
    val isPositive = diff <= 0
    val statusLabel = when {
        label == "건조함" && !isPositive -> "건조"
        label == "건조함" -> "개선"
        label == "유분 지수" && !isPositive -> "증가"
        label == "유분 지수" -> "조절됨"
        !isPositive -> "증가"
        else -> "감소"
    }
    val sign = if (diff > 0) "+" else ""
    return SkinChangeStat(label, "$sign$diff", statusLabel, isPositive)
}

@Composable
private fun GeneratedReportContent(
    modifier: Modifier = Modifier,
    calendarType: ReportCalendarType,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    skinLogViewModel: SkinLogViewModel,
    mealLogViewModel: MealLogViewModel,
    onBackToSelect: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBackToSelect),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = null, tint = CoralPrimary)
            Text(text = "기간 다시 선택", style = MaterialTheme.typography.bodyMedium, color = CoralPrimary, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "${rangeStart.monthValue}.${rangeStart.dayOfMonth} ~ ${rangeEnd.monthValue}.${rangeEnd.dayOfMonth} ${if (calendarType == ReportCalendarType.SKIN) "피부 리포트" else "식생활 리포트"}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (calendarType == ReportCalendarType.SKIN) {
            SkinPeriodReportContent(rangeStart = rangeStart, rangeEnd = rangeEnd, skinLogViewModel = skinLogViewModel)
        } else {
            FoodPeriodReportContent(rangeStart = rangeStart, rangeEnd = rangeEnd, mealLogViewModel = mealLogViewModel)
        }
    }
}

@Composable
private fun SkinPeriodReportContent(rangeStart: LocalDate, rangeEnd: LocalDate, skinLogViewModel: SkinLogViewModel) {
    val allSkinEntries by skinLogViewModel.entries.collectAsState()
    val dayCount = remember(rangeStart, rangeEnd) { ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1 }
    val rangedSkin = remember(allSkinEntries, rangeStart, rangeEnd) {
        allSkinEntries.filter { !it.loggedAt.toLocalDate().isBefore(rangeStart) && !it.loggedAt.toLocalDate().isAfter(rangeEnd) }
            .sortedBy { it.loggedAt }
    }
    val previousSkin = remember(allSkinEntries, rangeStart, dayCount) {
        val previousEnd = rangeStart.minusDays(1)
        val previousStart = rangeStart.minusDays(dayCount.toLong())
        allSkinEntries.filter { !it.loggedAt.toLocalDate().isBefore(previousStart) && !it.loggedAt.toLocalDate().isAfter(previousEnd) }
    }

    val first = rangedSkin.firstOrNull()
    val last = rangedSkin.lastOrNull()
    val hasEnoughHistory = rangedSkin.size >= 2 && first != null && last != null
    val skinChangeStats = if (hasEnoughHistory && first != null && last != null) {
        listOf(
            deltaStat("트러블 빈도", first.troubleLevel, last.troubleLevel),
            deltaStat("유분 지수", first.oilLevel, last.oilLevel),
            deltaStat("건조함", first.drynessLevel, last.drynessLevel)
        )
    } else {
        mockSkinChangeStats
    }

    val currentAverageScore = remember(rangedSkin) {
        if (rangedSkin.isEmpty()) 0 else rangedSkin.map { skinConditionScore(it) }.average().roundToInt()
    }
    val previousAverageScore = remember(previousSkin) {
        if (previousSkin.isEmpty()) null else previousSkin.map { skinConditionScore(it) }.average().roundToInt()
    }
    val trendValues = remember(rangedSkin) {
        val raw = rangedSkin.map { skinConditionScore(it) }
        if (raw.size > 10) bucketAverage(raw.map { it.roundToInt() }, 6) else raw
    }
    val improvements = remember(rangedSkin) { topSkinImprovements(rangedSkin) }
    val recommendedIngredients = remember(rangedSkin) { skinBasketIngredientsFor(rangedSkin) }

    BeforeAfterComparison(beforePhoto = first?.photo, afterPhoto = last?.photo)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        skinChangeStats.forEach { stat -> SkinChangeStatCard(stat = stat, modifier = Modifier.weight(1f)) }
    }

    TrendSection(values = trendValues, leftLabel = "${dayCount}일 전", rightLabel = "최근")
    TopImprovementsSection(items = improvements)
    SkinBasketSection(ingredients = recommendedIngredients)
    GoalTrackingSection(currentAverage = currentAverageScore, previousAverage = previousAverageScore, periodLabel = "이전 ${dayCount}일")
    ResearchInsightSection()
}

// Redesigned 2026-08-18 to match the user's 4-section paid-report spec (previously built into
// AnalysisScreen's 7일/30일 tabs, then moved here since this — the 리포트 tab's 캘린더 →
// 리포트 생성하기 flow — is what the user actually meant by "유료 리포트 화면"). Sodium and
// vegetable/fish/nut food-category metrics from the spec aren't computable (no such columns
// in the food table per the backend's own draft), so they're reframed onto the 4 nutrient axes
// the client actually tracks (vitamin C/E, omega-3, sugar) — see NutritionAxis below.
@Composable
private fun FoodPeriodReportContent(rangeStart: LocalDate, rangeEnd: LocalDate, mealLogViewModel: MealLogViewModel) {
    val allMealEntries by mealLogViewModel.entries.collectAsState()
    val dayCount = remember(rangeStart, rangeEnd) { ChronoUnit.DAYS.between(rangeStart, rangeEnd).toInt() + 1 }
    val rangedMeals = remember(allMealEntries, rangeStart, rangeEnd) {
        allMealEntries.filter { !it.eatenAt.toLocalDate().isBefore(rangeStart) && !it.eatenAt.toLocalDate().isAfter(rangeEnd) }
    }
    val previousMeals = remember(allMealEntries, rangeStart, dayCount) {
        val previousEnd = rangeStart.minusDays(1)
        val previousStart = rangeStart.minusDays(dayCount.toLong())
        allMealEntries.filter { !it.eatenAt.toLocalDate().isBefore(previousStart) && !it.eatenAt.toLocalDate().isAfter(previousEnd) }
    }

    val totalMeals = rangedMeals.size
    val averageScore = remember(rangedMeals) { if (rangedMeals.isEmpty()) 0 else SkinScoreCalculator.scoreForEntries(rangedMeals) }
    val previousAverageScore = remember(previousMeals) { if (previousMeals.isEmpty()) null else SkinScoreCalculator.scoreForEntries(previousMeals) }
    val deficiency = remember(rangedMeals) { SkinScoreCalculator.analyzeDeficiency(rangedMeals) }

    val omega3Count = remember(rangedMeals) {
        mealsWithNutrient(rangedMeals) { food, ratio -> food.omega3Mg?.let { it * ratio >= TARGET_OMEGA3_MG } == true }
    }
    val vitCMealCount = remember(rangedMeals) {
        mealsWithNutrient(rangedMeals) { food, ratio -> food.vitCMg?.let { it * ratio >= TARGET_VIT_C_MG } == true }
    }
    val vitEMealCount = remember(rangedMeals) {
        mealsWithNutrient(rangedMeals) { food, ratio -> food.vitEMg?.let { it * ratio >= TARGET_VIT_E_MG } == true }
    }
    val sugarSnackCount = remember(rangedMeals) { highSugarSnackCount(rangedMeals) }
    val highGiCount = remember(rangedMeals) { rangedMeals.flatMap { it.items }.count { it.food.isHighGi } }

    val axes = remember(rangedMeals, sugarSnackCount, totalMeals) { buildNutritionAxes(rangedMeals, sugarSnackCount, totalMeals) }
    val patterns = remember(rangedMeals) { detectDietPatterns(rangedMeals) }
    val goals = remember(deficiency, omega3Count, vitCMealCount, vitEMealCount, totalMeals, sugarSnackCount, highGiCount) {
        buildActionGoals(deficiency, omega3Count, vitCMealCount, vitEMealCount, totalMeals, sugarSnackCount, highGiCount)
    }

    HeroSummaryCard(dayCount = dayCount, averageScore = averageScore, previousAverageScore = previousAverageScore, axes = axes)

    DietPatternSection(
        dayCount = dayCount,
        stats = listOf(
            FrequencyStat("🍰", "당류 높은 간식", "${sugarSnackCount}회"),
            FrequencyStat("🐟", "오메가3 공급 식품 섭취", "${omega3Count}회"),
            FrequencyStat("🥦", "비타민C 공급 식품 포함 식사", "${vitCMealCount}/${totalMeals}끼"),
            FrequencyStat("🥜", "비타민E 공급 식품 섭취", "${vitEMealCount}회"),
            FrequencyStat("🍚", "정제 탄수화물(고GI) 식사", "${highGiCount}회")
        ),
        patterns = patterns
    )

    NutritionBalanceSection(axes = axes)

    TopActionsSection(dayCount = dayCount, goals = goals)
}

private fun skinConditionScore(entry: SkinLogEntry): Float {
    val sum = entry.troubleLevel + entry.oilLevel + entry.drynessLevel
    return ((15f - sum) / 12f * 100f).coerceIn(0f, 100f)
}

private fun topSkinImprovements(entries: List<SkinLogEntry>): List<String> {
    if (entries.isEmpty()) {
        return listOf(
            "꾸준한 피부 기록으로 변화 추이를 확인해보세요.",
            "항산화·오메가3 식품을 균형 있게 섭취해보세요.",
            "규칙적인 수면과 수분 섭취를 유지해보세요."
        )
    }
    val troubleAvg = entries.map { it.troubleLevel }.average()
    val oilAvg = entries.map { it.oilLevel }.average()
    val drynessAvg = entries.map { it.drynessLevel }.average()

    val ranked = listOf(
        troubleAvg to "트러블 진정에 도움을 줄 수 있는 아연·오메가3가 풍부한 식품을 늘려보세요.",
        oilAvg to "유분 밸런스 관리를 위해 아연이 풍부한 식품과 항산화 식품을 챙겨보세요.",
        drynessAvg to "피부 장벽 강화를 위해 오메가3와 비타민E가 풍부한 식품을 늘려보세요."
    ).sortedByDescending { it.first }.map { it.second }

    val fallback = listOf(
        "지금의 관리 루틴을 꾸준히 유지해보세요.",
        "충분한 수분 섭취로 피부 컨디션을 관리해보세요.",
        "규칙적인 생활 습관을 유지해보세요."
    )
    return (ranked + fallback).take(3)
}

private fun skinBasketIngredientsFor(entries: List<SkinLogEntry>): List<Ingredient> {
    val keys = if (entries.isEmpty()) {
        listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E, IngredientSeedData.KEY_ZINC)
    } else {
        val troubleAvg = entries.map { it.troubleLevel }.average()
        val oilAvg = entries.map { it.oilLevel }.average()
        val drynessAvg = entries.map { it.drynessLevel }.average()
        val ranked = listOf(
            troubleAvg to listOf(IngredientSeedData.KEY_ZINC, IngredientSeedData.KEY_OMEGA3),
            oilAvg to listOf(IngredientSeedData.KEY_ZINC, IngredientSeedData.KEY_VIT_C),
            drynessAvg to listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_E)
        ).sortedByDescending { it.first }
        (ranked[0].second + ranked[1].second).distinct()
    }
    return keys.mapNotNull { IngredientSeedData.primaryFor(it) }.distinct()
}

private fun bucketAverage(values: List<Int>, bucketCount: Int): List<Float> {
    if (values.isEmpty()) return emptyList()
    val chunkSize = (values.size + bucketCount - 1) / bucketCount
    return values.chunked(chunkSize).map { chunk -> chunk.map { it.toFloat() }.average().toFloat() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
}

// --- Food-calendar report content: 4-section paid-report design (Skin Food Score summary,
// diet-pattern frequencies + narrative insights, a 4-axis nutrition radar, and a Top 3
// current->target action plan). See the comment above FoodPeriodReportContent for context.
private enum class AxisStatus(val emoji: String, val label: String, val background: Color, val foreground: Color) {
    GOOD("🟢", "충분", PositiveTagBackground, PositiveTagText),
    MODERATE("🟡", "부족", CautionTagBackground, CautionTagText),
    CONCERN("🔴", "주의", WarningTagBackground, WarningTagText)
}

private val bucketHeaders = mapOf(
    AxisStatus.GOOD to "잘하고 있어요",
    AxisStatus.MODERATE to "조금 부족해요",
    AxisStatus.CONCERN to "주의가 필요해요"
)

private data class NutritionAxis(
    val code: String,
    val label: String,
    val value: Float,
    val status: AxisStatus,
    val why: String,
    val myRecord: String
)

private data class DietPattern(val title: String, val detail: String, val confidence: String, val sampleSize: Int)

private data class ActionGoal(val emoji: String, val title: String, val current: String, val target: String)

private const val TARGET_VIT_C_MG = 30.0
private const val TARGET_VIT_E_MG = 3.0
private const val TARGET_OMEGA3_MG = 500.0

private fun mealsWithNutrient(entries: List<MealLogEntry>, predicate: (Food, Double) -> Boolean): Int =
    entries.count { entry -> entry.items.any { predicate(it.food, it.portionRatio) } }

private fun highSugarSnackCount(entries: List<MealLogEntry>): Int =
    entries.count { it.mealType == MealType.SNACK && it.items.any { item -> item.food.sugarG * item.portionRatio >= 15.0 } }

private data class RatioResult(val value: Float, val status: AxisStatus)

private fun ratioResult(average: Double?, target: Double): RatioResult {
    if (average == null) return RatioResult(0f, AxisStatus.CONCERN)
    val ratio = average / target
    val status = when {
        ratio >= 0.8 -> AxisStatus.GOOD
        ratio >= 0.4 -> AxisStatus.MODERATE
        else -> AxisStatus.CONCERN
    }
    val value = ratio.coerceIn(0.0, 1.2).toFloat() / 1.2f
    return RatioResult(value, status)
}

private fun concernRatioResult(rate: Double): RatioResult {
    val value = (1.0 - rate).coerceIn(0.0, 1.0).toFloat()
    val status = when {
        rate <= 0.15 -> AxisStatus.GOOD
        rate <= 0.35 -> AxisStatus.MODERATE
        else -> AxisStatus.CONCERN
    }
    return RatioResult(value, status)
}

private fun buildNutritionAxes(entries: List<MealLogEntry>, sugarSnackCount: Int, totalMeals: Int): List<NutritionAxis> {
    val vitC = SkinScoreCalculator.averageNutrient(entries) { it.vitCMg }
    val vitE = SkinScoreCalculator.averageNutrient(entries) { it.vitEMg }
    val omega3 = SkinScoreCalculator.averageNutrient(entries) { it.omega3Mg }
    val sugarRate = if (totalMeals == 0) 0.0 else sugarSnackCount.toDouble() / totalMeals

    val vitCResult = ratioResult(vitC.average, TARGET_VIT_C_MG)
    val vitEResult = ratioResult(vitE.average, TARGET_VIT_E_MG)
    val omega3Result = ratioResult(omega3.average, TARGET_OMEGA3_MG)
    val sugarResult = concernRatioResult(sugarRate)

    return listOf(
        NutritionAxis(
            code = "VIT_C",
            label = "비타민C",
            value = vitCResult.value,
            status = vitCResult.status,
            why = "비타민C는 콜라겐 합성 과정의 필수 보조인자로 잘 알려져 있어요.",
            myRecord = vitC.average?.let { "평균 섭취량 %.0fmg (기준 %.0fmg)".format(it, TARGET_VIT_C_MG) } ?: "관련 데이터가 아직 부족해요"
        ),
        NutritionAxis(
            code = "VIT_E",
            label = "비타민E",
            value = vitEResult.value,
            status = vitEResult.status,
            why = "비타민E는 대표적인 지용성 항산화 성분으로 연구되는 영양 요인이에요.",
            myRecord = vitE.average?.let { "평균 섭취량 %.1fmg (기준 %.1fmg)".format(it, TARGET_VIT_E_MG) } ?: "관련 데이터가 아직 부족해요"
        ),
        NutritionAxis(
            code = "OMEGA3",
            label = "오메가3",
            value = omega3Result.value,
            status = omega3Result.status,
            why = "오메가-3 지방산은 피부 장벽·항염과 관련해 연구되는 영양 요인 중 하나예요.",
            myRecord = omega3.average?.let { "평균 섭취량 %.0fmg (기준 %.0fmg)".format(it, TARGET_OMEGA3_MG) } ?: "관련 데이터가 아직 부족해요"
        ),
        NutritionAxis(
            code = "SUGAR",
            label = "당류 관리",
            value = sugarResult.value,
            status = sugarResult.status,
            why = "당류를 과다 섭취하면 '최종당화산물(AGEs)' 생성이 늘어 콜라겐 섬유의 탄력이 저하될 수 있다는 연구들이 있어요.",
            myRecord = "당류 높은 간식 ${sugarSnackCount}회 / 전체 ${totalMeals}끼"
        )
    )
}

// Mirrors the backend's observedPatterns contract: hedge tone only, no causal wording,
// always carries a confidenceLevel (low/medium — never "high") and a sampleSize.
private fun detectDietPatterns(entries: List<MealLogEntry>): List<DietPattern> {
    val patterns = mutableListOf<DietPattern>()

    val dinners = entries.filter { it.mealType == MealType.DINNER }
    val highSugarDinners = dinners.count { entry -> entry.items.any { item -> item.food.sugarG * item.portionRatio >= 15.0 } }
    if (dinners.size >= 3 && highSugarDinners.toDouble() / dinners.size >= 0.5) {
        patterns.add(
            DietPattern(
                title = "저녁 식사에서 당류 높은 음식이 반복됐어요.",
                detail = "${dinners.size}번의 저녁 중 ${highSugarDinners}번에서 당류가 높은 음식이 있었어요.",
                confidence = if (dinners.size >= 7) "medium" else "low",
                sampleSize = dinners.size
            )
        )
    }

    val weekday = entries.filter { it.eatenAt.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    val weekend = entries.filter { it.eatenAt.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    fun sugarRateOf(list: List<MealLogEntry>): Double =
        if (list.isEmpty()) 0.0 else list.count { entry -> entry.items.any { item -> item.food.sugarG * item.portionRatio >= 15.0 } }.toDouble() / list.size
    val weekdayRate = sugarRateOf(weekday)
    val weekendRate = sugarRateOf(weekend)
    if (weekday.size >= 3 && weekend.size >= 2 && weekendRate - weekdayRate >= 0.2) {
        patterns.add(
            DietPattern(
                title = "주말에 당류 섭취가 늘었어요.",
                detail = "평일 당류 높은 식사 비율(${(weekdayRate * 100).roundToInt()}%)보다 주말 비율(${(weekendRate * 100).roundToInt()}%)이 더 높았어요.",
                confidence = if (weekday.size + weekend.size >= 14) "medium" else "low",
                sampleSize = weekday.size + weekend.size
            )
        )
    }

    return patterns.ifEmpty {
        listOf(DietPattern("아직 뚜렷한 반복 패턴은 발견되지 않았어요.", "기록이 더 쌓이면 패턴을 찾아드릴게요.", "low", entries.size))
    }
}

private fun buildActionGoals(
    deficiency: SkinScoreCalculator.DeficiencySummary,
    omega3Count: Int,
    vitCMealCount: Int,
    vitEMealCount: Int,
    totalMeals: Int,
    sugarSnackCount: Int,
    highGiCount: Int
): List<ActionGoal> {
    val candidates = mutableListOf<Pair<Int, ActionGoal>>()

    if (sugarSnackCount >= 2) {
        candidates.add(4 to ActionGoal("🍰", "단 간식 횟수 줄이기", "${sugarSnackCount}회", "${maxOf(0, sugarSnackCount - 2)}회 이하"))
    }
    if (highGiCount >= 3) {
        candidates.add(3 to ActionGoal("🍚", "정제 탄수화물 식사 줄이기", "${highGiCount}회", "${maxOf(0, highGiCount - 2)}회 이하"))
    }
    if (deficiency.omega3Deficient) {
        candidates.add(3 to ActionGoal("🐟", "오메가3 공급 식품 추가하기", "${omega3Count}회", "${omega3Count + 2}회"))
    }
    if (deficiency.vitCDeficient) {
        candidates.add(2 to ActionGoal("🥦", "비타민C 공급 식품 늘리기", "${vitCMealCount}/${totalMeals}끼", "${minOf(totalMeals, vitCMealCount + 3)}/${totalMeals}끼"))
    }
    if (deficiency.vitEDeficient) {
        candidates.add(2 to ActionGoal("🥜", "비타민E 공급 식품 추가하기", "${vitEMealCount}회", "${vitEMealCount + 2}회"))
    }

    val fallback = listOf(
        ActionGoal("✅", "지금의 균형 잡힌 식습관 유지하기", "현재 상태", "계속 유지"),
        ActionGoal("💧", "수분 섭취 습관 유지하기", "현재 상태", "계속 유지"),
        ActionGoal("🥗", "다양한 색깔의 재료 골고루 먹기", "현재 상태", "계속 유지")
    )
    val ranked = candidates.sortedByDescending { it.first }.map { it.second }
    return (ranked + fallback).take(3)
}

@Composable
private fun HeroSummaryCard(dayCount: Int, averageScore: Int, previousAverageScore: Int?, axes: List<NutritionAxis>) {
    val grouped = axes.groupBy { it.status }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "최근 ${dayCount}일 Skin Food Score", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${averageScore}점", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (previousAverageScore != null) {
                    val diff = averageScore - previousAverageScore
                    val arrow = if (diff > 0) "▲" else if (diff < 0) "▼" else "-"
                    "이전 기간 ${previousAverageScore} → ${averageScore} ${arrow}${kotlin.math.abs(diff)}"
                } else {
                    "이전 기간과 비교할 기록이 아직 부족해요"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(AxisStatus.GOOD, AxisStatus.MODERATE, AxisStatus.CONCERN).forEach { status ->
                    val labels = grouped[status]?.map { it.label } ?: emptyList()
                    if (labels.isNotEmpty()) {
                        Column {
                            Text(
                                text = "${status.emoji} ${bucketHeaders[status]}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                labels.forEach { label -> AxisTagChip(text = label, status = status) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AxisTagChip(text: String, status: AxisStatus) {
    Surface(shape = RoundedCornerShape(50), color = status.background) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = status.foreground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private data class FrequencyStat(val emoji: String, val label: String, val count: String)

@Composable
private fun DietPatternSection(dayCount: Int, stats: List<FrequencyStat>, patterns: List<DietPattern>) {
    Column {
        SectionTitle("내 식생활 패턴 분석")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "최근 ${dayCount}일 동안", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.forEach { stat ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${stat.emoji} ${stat.label}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(text = stat.count, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "🔍 반복 패턴을 발견했어요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                patterns.forEach { pattern ->
                    Column {
                        Text(text = pattern.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = pattern.detail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "신뢰도: ${if (pattern.confidence == "medium") "보통" else "낮음"} · 표본 ${pattern.sampleSize}건",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionBalanceSection(axes: List<NutritionAxis>) {
    var expandedCode by remember { mutableStateOf<String?>(null) }

    Column {
        SectionTitle("🧬 Skin Nutrition Balance")
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp)) {
                RadarChart(axes = axes, modifier = Modifier.fillMaxWidth().height(200.dp))
                Spacer(modifier = Modifier.height(16.dp))
                axes.chunked(2).forEach { rowAxes ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowAxes.forEach { axis ->
                            AxisSummaryRow(
                                axis = axis,
                                expanded = expandedCode == axis.code,
                                onClick = { expandedCode = if (expandedCode == axis.code) null else axis.code },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowAxes.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                val expandedAxis = axes.find { it.code == expandedCode }
                if (expandedAxis != null) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SurfaceMuted) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "왜 확인하나요?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = expandedAxis.why, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "내 기록에서는?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = expandedAxis.myRecord, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AxisSummaryRow(axis: NutritionAxis, expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (expanded) axis.status.background else SurfaceMuted
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = axis.status.emoji, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = axis.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = axis.status.label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun RadarChart(axes: List<NutritionAxis>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val n = axes.size
        if (n < 3) return@Canvas
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.75f
        val angleStep = (2 * PI / n)

        for (ring in 1..4) {
            val r = radius * ring / 4f
            val ringPath = Path()
            for (i in 0 until n) {
                val angle = -PI / 2 + i * angleStep
                val point = Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat())
                if (i == 0) ringPath.moveTo(point.x, point.y) else ringPath.lineTo(point.x, point.y)
            }
            ringPath.close()
            drawPath(path = ringPath, color = SurfaceMuted, style = Stroke(width = 1.dp.toPx()))
        }

        for (i in 0 until n) {
            val angle = -PI / 2 + i * angleStep
            val point = Offset(center.x + (radius * cos(angle)).toFloat(), center.y + (radius * sin(angle)).toFloat())
            drawLine(color = SurfaceMuted, start = center, end = point, strokeWidth = 1.dp.toPx())
        }

        val valuePath = Path()
        axes.forEachIndexed { i, axis ->
            val angle = -PI / 2 + i * angleStep
            val r = radius * axis.value.coerceIn(0f, 1f)
            val point = Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat())
            if (i == 0) valuePath.moveTo(point.x, point.y) else valuePath.lineTo(point.x, point.y)
        }
        valuePath.close()
        drawPath(path = valuePath, color = CoralPrimary.copy(alpha = 0.25f))
        drawPath(path = valuePath, color = CoralPrimary, style = Stroke(width = 2.dp.toPx()))

        axes.forEachIndexed { i, axis ->
            val angle = -PI / 2 + i * angleStep
            val r = radius * axis.value.coerceIn(0f, 1f)
            val point = Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat())
            drawCircle(color = CoralPrimary, radius = 4.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun TopActionsSection(dayCount: Int, goals: List<ActionGoal>) {
    Column {
        SectionTitle("⭐ 가장 중요한 3가지")
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "다음 ${dayCount}일은 이것만 바꿔보세요.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            goals.forEachIndexed { index, goal -> ActionGoalCard(rank = index + 1, goal = goal) }
        }
    }
}

@Composable
private fun ActionGoalCard(rank: Int, goal: ActionGoal) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${goal.emoji} ${rank}. ${goal.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "현재", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = goal.current, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(text = "→", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "목표", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = goal.target, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CoralPrimary)
                }
            }
        }
    }
}

@Composable
private fun TrendSection(values: List<Float>, leftLabel: String, rightLabel: String) {
    Column {
        SectionTitle("점수 변화 추이")
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (values.size >= 2) {
                    TrendLineChart(values = values, modifier = Modifier.fillMaxWidth().height(140.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = leftLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(text = rightLabel, style = MaterialTheme.typography.labelSmall, color = CoralPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(text = "추이를 보여줄 만큼 기록이 아직 충분하지 않아요.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun TopImprovementsSection(items: List<String>) {
    Column {
        SectionTitle("개인화된 우선 개선사항 Top 3")
        Spacer(modifier = Modifier.height(12.dp))
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
}

@Composable
private fun SkinBasketSection(ingredients: List<Ingredient>) {
    Column {
        SectionTitle("기간 전체를 고려한 Skin Basket")
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ingredients.forEach { ingredient -> IngredientMiniCard(ingredient = ingredient, modifier = Modifier.weight(1f)) }
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
private fun GoalTrackingSection(currentAverage: Int, previousAverage: Int?, periodLabel: String, targetScore: Int = 75) {
    val achieved = currentAverage >= targetScore
    val badgeBackground = if (achieved) PositiveTagBackground else WarningTagBackground
    val badgeText = if (achieved) PositiveTagText else WarningTagText

    Column {
        SectionTitle("지난 목표 달성 여부 및 변화 추적")
        Spacer(modifier = Modifier.height(12.dp))
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
                        "${periodLabel} 대비 ${sign}${diff}점 변화했어요."
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
}

@Composable
private fun ResearchInsightSection() {
    Column {
        SectionTitle("논문 근거 기반 상세 리포트")
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                researchInsights.forEach { text -> Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
                Text(
                    text = "특정 논문을 인용한 것이 아닌, 널리 알려진 일반 영양학적 지식을 바탕으로 구성됐어요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun BeforeAfterComparison(beforePhoto: Bitmap?, afterPhoto: Bitmap?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            BeforeAfterHalf(
                label = "Before",
                photo = beforePhoto,
                placeholderBackground = SurfaceMuted,
                badgeColor = Color(0xFF5C5850),
                badgeAlignment = Alignment.TopStart,
                modifier = Modifier.weight(1f)
            )
            BeforeAfterHalf(
                label = "After",
                photo = afterPhoto,
                placeholderBackground = Color(0xFFF5D9C8),
                badgeColor = CoralPrimary,
                badgeAlignment = Alignment.TopEnd,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BeforeAfterHalf(
    label: String,
    photo: Bitmap?,
    placeholderBackground: Color,
    badgeColor: Color,
    badgeAlignment: Alignment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = badgeAlignment
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder until a real 피부기록 photo exists for this slot.
            Box(modifier = Modifier.fillMaxSize().background(placeholderBackground))
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SkinChangeStatCard(stat: SkinChangeStat, modifier: Modifier = Modifier) {
    val tagBackground = if (stat.isPositive) PositiveTagBackground else WarningTagBackground
    val tagText = if (stat.isPositive) PositiveTagText else WarningTagText
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stat.label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(50), color = tagBackground) {
                Text(
                    text = stat.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = tagText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
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

        // Baseline
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
        points.forEach { point ->
            drawCircle(color = CoralPrimary, radius = 4.dp.toPx(), center = point)
        }
    }
}
