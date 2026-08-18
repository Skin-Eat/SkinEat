package com.example.lionideaton.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.data.model.MealType
import com.example.lionideaton.data.model.UserProfile
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CautionTagBackground
import com.example.lionideaton.ui.theme.CautionTagText
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.GaugeOrange
import com.example.lionideaton.ui.theme.GaugeRed
import com.example.lionideaton.ui.theme.GaugeTrack
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class TodayFoodLog(
    val name: String,
    val skinTag: String,
    val mealTime: String,
    val isWarning: Boolean
)

private val homeTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private const val ANTIOXIDANT_VIT_C_TARGET = 100.0
private const val ANTIOXIDANT_VIT_E_TARGET = 15.0
private const val COLLAGEN_PROTEIN_TARGET = 60.0
private const val COLLAGEN_ZINC_TARGET = 8.0

// How many days back the swipeable calendar can go — bounded by how much seeded/real history exists.
private const val DAY_WINDOW = 14

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogFoodClick: () -> Unit = {},
    onSkinScoreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    mealLogViewModel: MealLogViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val profile by userProfileViewModel.profile.collectAsState()
    val today = remember { LocalDate.now() }
    val pagerState = rememberPagerState(initialPage = DAY_WINDOW - 1) { DAY_WINDOW }
    val selectedDate = today.minusDays((DAY_WINDOW - 1 - pagerState.currentPage).toLong())
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        HomeCalendarHeader(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                val page = DAY_WINDOW - 1 - ChronoUnit.DAYS.between(date, today).toInt()
                coroutineScope.launch { pagerState.animateScrollToPage(page.coerceIn(0, DAY_WINDOW - 1)) }
            },
            onProfileClick = onProfileClick
        )

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val date = today.minusDays((DAY_WINDOW - 1 - page).toLong())
            HomeDayContent(
                date = date,
                profile = profile,
                mealLogViewModel = mealLogViewModel,
                onLogFoodClick = onLogFoodClick,
                onSkinScoreClick = onSkinScoreClick
            )
        }
    }
}

@Composable
private fun HomeCalendarHeader(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onProfileClick: () -> Unit
) {
    val monday = selectedDate.with(DayOfWeek.MONDAY)
    val weekDates = (0..6).map { monday.plusDays(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selectedDate.monthValue}.${selectedDate.dayOfMonth} ${koreanWeekdayShort(selectedDate)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = "알림", tint = TextSecondary)
                }
                Surface(
                    shape = CircleShape,
                    color = CoralPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onProfileClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "마이페이지", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDates.forEach { date ->
                DayChip(date = date, selected = date == selectedDate, onClick = { onDateSelected(date) })
            }
        }
    }
}

@Composable
private fun DayChip(date: LocalDate, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text = koreanWeekdayShort(date), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = CircleShape,
            color = if (selected) CoralPrimary else Color.Transparent
        ) {
            Text(
                text = "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else TextPrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

private fun koreanWeekdayShort(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일"
}

@Composable
private fun HomeDayContent(
    date: LocalDate,
    profile: UserProfile,
    mealLogViewModel: MealLogViewModel,
    onLogFoodClick: () -> Unit,
    onSkinScoreClick: () -> Unit
) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val dayEntries = remember(allEntries, date) { mealLogViewModel.entriesForDate(date) }
    val previousDayEntries = remember(allEntries, date) { mealLogViewModel.entriesForDate(date.minusDays(1)) }
    val dayItems = dayEntries.flatMap { it.items }

    val score = SkinScoreCalculator.scoreForEntries(dayEntries)
    val previousScore = SkinScoreCalculator.scoreForEntries(previousDayEntries)

    val vitC = SkinScoreCalculator.averageNutrient(dayEntries) { it.vitCMg }.average ?: 0.0
    val vitE = SkinScoreCalculator.averageNutrient(dayEntries) { it.vitEMg }.average ?: 0.0
    val protein = SkinScoreCalculator.averageNutrient(dayEntries) { it.proteinG }.average ?: 0.0
    val zinc = SkinScoreCalculator.averageNutrient(dayEntries) { it.zincMg }.average ?: 0.0

    val antioxidantIndex = (((vitC / ANTIOXIDANT_VIT_C_TARGET) * 50) + ((vitE / ANTIOXIDANT_VIT_E_TARGET) * 50))
        .roundToInt().coerceIn(0, 100)
    val collagenIndex = (((protein / COLLAGEN_PROTEIN_TARGET) * 50) + ((zinc / COLLAGEN_ZINC_TARGET) * 50))
        .roundToInt().coerceIn(0, 100)
    val hasInflammationConcern = dayItems.any { item ->
        (item.food.satFatG?.let { it * item.portionRatio >= 5.0 } == true) || (item.food.sugarG * item.portionRatio >= 15.0)
    }
    // Placeholder until real water-intake tracking exists — there's no hydration field anywhere
    // in the schema yet, so this is only a rough stand-in, not a measured value.
    val hydrationIndex = (70 + dayEntries.size * 5).coerceAtMost(100)

    val comment = when {
        dayEntries.isEmpty() -> "이 날 기록된 식사가 없어요."
        hasInflammationConcern -> "이 날 포화지방·당류가 높은 식사가 있었어요. 채소와 오메가3 공급 식품을 함께 섭취해보세요."
        else -> "이 날 섭취한 식단이 전반적으로 균형 잡혀 있어요."
    }

    val dayFoods = dayEntries.sortedBy { it.eatenAt }.flatMap { entry ->
        entry.items.map { item ->
            val itemScore = SkinScoreCalculator.scoreForItem(item)
            TodayFoodLog(
                name = item.food.name,
                skinTag = when {
                    itemScore >= 80 -> "Skin Friendly"
                    itemScore >= 60 -> "보통"
                    else -> "High Sodium ⚠"
                },
                mealTime = "${entry.mealType.label} ${entry.eatenAt.toLocalTime().format(homeTimeFormatter)}",
                isWarning = itemScore < 60
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkinScoreCard(
            skinType = profile.skinType?.let { "${it.label} 피부" } ?: "피부타입 미설정",
            score = score,
            comment = comment,
            onClick = onSkinScoreClick
        )

        MealSlotRow(dayEntries = dayEntries, onAddClick = onLogFoodClick)

        if (dayFoods.isNotEmpty()) {
            TodayFoodLogSection(foods = dayFoods)
        }

        NutrientStatusSection(
            antioxidant = antioxidantIndex,
            collagenSynthesis = collagenIndex,
            hasInflammationConcern = hasInflammationConcern,
            hydration = hydrationIndex
        )

        SkinFoodScoreCard(score = score, previousScore = previousScore, hasInflammationConcern = hasInflammationConcern, antioxidant = antioxidantIndex)

        if (dayItems.isNotEmpty()) {
            FoodNutrientDetailSection(items = dayItems)
        }

        DailyFeedbackCard(hasInflammationConcern = hasInflammationConcern, antioxidant = antioxidantIndex)

        RecommendedIngredientsRow()

        LogTodayFoodButton(onClick = onLogFoodClick)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SkinScoreCard(skinType: String, score: Int, comment: String, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "오늘의 피부 점수",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                SkinTypeTag(skinType)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkinScoreGauge(score = score, maxScore = 100, modifier = Modifier.size(160.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = comment,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SkinTypeTag(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SkinScoreGauge(score: Int, maxScore: Int, modifier: Modifier = Modifier) {
    val progress = (score.toFloat() / maxScore).coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 150f
            val maxSweep = 240f
            drawArc(
                color = GaugeTrack,
                startAngle = startAngle,
                sweepAngle = maxSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(GaugeOrange, GaugeRed),
                    center = Offset(size.width / 2f, size.height / 2f)
                ),
                startAngle = startAngle,
                sweepAngle = maxSweep * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = "/ $maxScore 점", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MealSlotRow(dayEntries: List<MealLogEntry>, onAddClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MealType.entries.forEach { type ->
            val entry = dayEntries.firstOrNull { it.mealType == type }
            MealSlotCard(
                label = type.label,
                foodName = entry?.items?.firstOrNull()?.food?.name,
                onClick = onAddClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MealSlotCard(label: String, foodName: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = SurfaceMuted) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (foodName != null) Icons.Filled.Restaurant else Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (foodName != null) CoralPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = CoralPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                text = foodName ?: "추가하기",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TodayFoodLogSection(foods: List<TodayFoodLog>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "오늘 먹은 음식 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = "전체보기", style = MaterialTheme.typography.bodySmall, color = CoralPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            foods.forEach { food -> FoodLogRow(food) }
        }
    }
}

@Composable
private fun FoodLogRow(food: TodayFoodLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodThumbnail()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = food.mealTime, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FoodTag(text = food.skinTag, isWarning = food.isWarning)
        }
    }
}

// Placeholder thumbnail until real food photos are wired up.
@Composable
private fun FoodThumbnail() {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FoodTag(text: String, isWarning: Boolean) {
    val backgroundColor = if (isWarning) WarningTagBackground else PositiveTagBackground
    val textColor = if (isWarning) WarningTagText else PositiveTagText
    Surface(shape = RoundedCornerShape(50), color = backgroundColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun NutrientStatusSection(antioxidant: Int, collagenSynthesis: Int, hasInflammationConcern: Boolean, hydration: Int) {
    Column {
        Text(
            text = "오늘 부족/과잉 영양소 확인",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NutrientStatusBar(label = "항산화지수", progress = antioxidant / 100f, statusText = "${statusWordFor(antioxidant)} $antioxidant%", color = PositiveTagText)
                NutrientStatusBar(label = "콜라겐합성", progress = collagenSynthesis / 100f, statusText = "${statusWordFor(collagenSynthesis)} $collagenSynthesis%", color = GaugeOrange)
                NutrientStatusBar(
                    label = "염증지수",
                    progress = if (hasInflammationConcern) 0.85f else 0.2f,
                    statusText = if (hasInflammationConcern) "주의 높음" else "양호",
                    color = if (hasInflammationConcern) WarningTagText else PositiveTagText
                )
                NutrientStatusBar(label = "수분", progress = hydration / 100f, statusText = "${statusWordFor(hydration)} $hydration%", color = CautionTagText)
            }
        }
    }
}

private fun statusWordFor(value: Int): String = when {
    value >= 80 -> "충분"
    value >= 50 -> "보통"
    else -> "부족"
}

@Composable
private fun NutrientStatusBar(label: String, progress: Float, statusText: String, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = statusText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = SurfaceMuted
        )
    }
}

@Composable
private fun SkinFoodScoreCard(score: Int, previousScore: Int, hasInflammationConcern: Boolean, antioxidant: Int) {
    val grade = gradeFor(score)
    val delta = score - previousScore
    val deltaText = when {
        delta > 0 -> "전날 대비 +${delta}점 상승"
        delta < 0 -> "전날 대비 ${delta}점 하락"
        else -> "전날과 동일"
    }

    Column {
        Text(
            text = "오늘의 Skin Food Score",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = PositiveTagBackground, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = grade, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CoralPrimary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "오늘 식단 피부 점수 ${score}점", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (antioxidant >= 70) {
                            MiniStatusChip(text = "✓ 항산화 우수", background = PositiveTagBackground, textColor = PositiveTagText)
                        }
                        if (hasInflammationConcern) {
                            MiniStatusChip(text = "⚠ 자극 성분 주의", background = WarningTagBackground, textColor = WarningTagText)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = deltaText, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

private fun gradeFor(score: Int): String = when {
    score >= 90 -> "A+"
    score >= 85 -> "A"
    score >= 75 -> "B+"
    score >= 65 -> "B"
    score >= 55 -> "C+"
    score >= 45 -> "C"
    else -> "D"
}

@Composable
private fun MiniStatusChip(text: String, background: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FoodNutrientDetailSection(items: List<MealItem>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "음식별 영양성분 확인",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = "상세보기", style = MaterialTheme.typography.bodySmall, color = CoralPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item -> FoodNutrientRow(item) }
        }
    }
}

@Composable
private fun FoodNutrientRow(item: MealItem) {
    val tags = nutrientTagsFor(item)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodThumbnail()
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.food.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        val isConcern = tag.endsWith("↑↑")
                        MiniStatusChip(
                            text = tag,
                            background = if (isConcern) WarningTagBackground else PositiveTagBackground,
                            textColor = if (isConcern) WarningTagText else PositiveTagText
                        )
                    }
                }
            }
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

private fun nutrientTagsFor(item: MealItem): List<String> {
    val food = item.food
    val ratio = item.portionRatio
    val tags = mutableListOf<String>()
    food.omega3Mg?.let { if (it * ratio >= 500.0) tags.add("오메가3↑") }
    food.vitCMg?.let { if (it * ratio >= 30.0) tags.add("비타민C↑") }
    food.vitEMg?.let { if (it * ratio >= 3.0) tags.add("비타민E↑") }
    if (food.sugarG * ratio >= 15.0) tags.add("당류↑↑")
    food.satFatG?.let { if (it * ratio >= 5.0) tags.add("포화지방↑↑") }
    return tags.take(2).ifEmpty { listOf("일반 식품") }
}

@Composable
private fun DailyFeedbackCard(hasInflammationConcern: Boolean, antioxidant: Int) {
    val feedback = buildString {
        if (hasInflammationConcern) {
            append("오늘 당류·포화지방이 높은 식사가 있어 피부에 자극이 될 수 있어요. ")
        } else {
            append("오늘은 전반적으로 균형 잡힌 식사를 하셨어요. ")
        }
        if (antioxidant >= 70) {
            append("항산화 식품도 잘 챙기셨네요! ")
        }
        append("남은 하루는 채소와 수분 섭취를 늘려보세요.")
    }

    Column {
        Text(
            text = "간단한 오늘의 피드백",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "오늘의 피부 코치", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CoralPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = feedback, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = SurfaceMuted) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "저녁 식사 전 물 한 잔으로 나트륨 배출을 도와보세요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private data class BasicIngredientTip(val name: String, val benefit: String)

private val basicIngredientTips = listOf(
    BasicIngredientTip("브로콜리", "항산화 최고"),
    BasicIngredientTip("연어", "오메가3 풍부"),
    BasicIngredientTip("블루베리", "비타민C 보충"),
    BasicIngredientTip("아보카도", "건강한 지방")
)

@Composable
private fun RecommendedIngredientsRow() {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "기본 식재료 추천",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = "더보기", style = MaterialTheme.typography.bodySmall, color = CoralPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            basicIngredientTips.forEach { tip ->
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = CircleShape, color = SurfaceMuted, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Restaurant, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = tip.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = tip.benefit, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogTodayFoodButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "오늘의 음식 기록하기", fontWeight = FontWeight.Bold)
    }
}
