package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.FoodSeedData
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CautionTagBackground
import com.example.lionideaton.ui.theme.CautionTagText
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText
import java.time.format.DateTimeFormatter

private data class FoodMenuItem(val name: String)

private val mockFoodMenu = FoodSeedData.foods.map { FoodMenuItem(it.name) }

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun FoodLogScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodLogViewModel = viewModel(),
    mealLogViewModel: MealLogViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFoodName by remember { mutableStateOf<String?>(null) }
    var servingCount by remember { mutableStateOf(1) }
    var foodPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var nutritionLabelPhoto by remember { mutableStateOf<Bitmap?>(null) }

    val photoAnalysisState by viewModel.photoAnalysisState.collectAsState()
    val allEntries by mealLogViewModel.entries.collectAsState()
    val todayMeals = remember(allEntries) { mealLogViewModel.todayEntries() }

    val foodPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            foodPhoto = bitmap
            viewModel.analyzeFoodPhoto(bitmap)
        }
    }
    val nutritionLabelLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) nutritionLabelPhoto = bitmap
    }

    val filteredMenu = remember(searchQuery) {
        if (searchQuery.isBlank()) mockFoodMenu else mockFoodMenu.filter { it.name.contains(searchQuery) }
    }

    fun registerMeal(name: String) {
        mealLogViewModel.registerMeal(foodName = name, servingCount = servingCount)
        selectedFoodName = null
        servingCount = 1
        searchQuery = ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PhotoCaptureSection(
            foodPhoto = foodPhoto,
            nutritionLabelPhoto = nutritionLabelPhoto,
            photoAnalysisState = photoAnalysisState,
            onCaptureFoodPhoto = { foodPhotoLauncher.launch(null) },
            onCaptureNutritionLabel = { nutritionLabelLauncher.launch(null) },
            onRetryAnalysis = { viewModel.retryAnalysis() },
            onCandidateSelected = { name ->
                selectedFoodName = name
                servingCount = 1
                viewModel.resetPhotoAnalysis()
            }
        )

        MenuSearchSection(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            results = filteredMenu,
            onFoodSelected = { name ->
                selectedFoodName = name
                servingCount = 1
            }
        )

        selectedFoodName?.let { name ->
            ServingAmountSection(
                foodName = name,
                servingCount = servingCount,
                onIncrease = { servingCount++ },
                onDecrease = { if (servingCount > 1) servingCount-- },
                onRegister = { registerMeal(name) }
            )
        }

        if (todayMeals.isNotEmpty()) {
            RegisteredMealsSection(meals = todayMeals)
        }
    }
}

@Composable
private fun PhotoCaptureSection(
    foodPhoto: Bitmap?,
    nutritionLabelPhoto: Bitmap?,
    photoAnalysisState: FoodPhotoAnalysisState,
    onCaptureFoodPhoto: () -> Unit,
    onCaptureNutritionLabel: () -> Unit,
    onRetryAnalysis: () -> Unit,
    onCandidateSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "사진으로 빠르게 기록하기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PhotoCaptureCard(
                title = "음식 사진 촬영",
                icon = Icons.Filled.PhotoCamera,
                capturedPhoto = foodPhoto,
                isAnalyzing = photoAnalysisState is FoodPhotoAnalysisState.Loading,
                statusText = when (photoAnalysisState) {
                    is FoodPhotoAnalysisState.Loading -> "AI 분석 중..."
                    is FoodPhotoAnalysisState.Success -> "AI 분석 완료"
                    is FoodPhotoAnalysisState.Error -> "분석 실패"
                    FoodPhotoAnalysisState.Idle -> if (foodPhoto != null) "촬영 완료" else null
                },
                onClick = onCaptureFoodPhoto,
                modifier = Modifier.weight(1f)
            )
            PhotoCaptureCard(
                title = "영양성분표 촬영",
                icon = Icons.Filled.Receipt,
                capturedPhoto = nutritionLabelPhoto,
                isAnalyzing = false,
                statusText = if (nutritionLabelPhoto != null) "촬영 완료 · AI 분석 준비중" else null,
                onClick = onCaptureNutritionLabel,
                modifier = Modifier.weight(1f)
            )
        }
        when (photoAnalysisState) {
            is FoodPhotoAnalysisState.Success -> {
                Spacer(modifier = Modifier.height(10.dp))
                AiFoodSuggestionCard(candidates = photoAnalysisState.candidates, onCandidateSelected = onCandidateSelected)
            }
            is FoodPhotoAnalysisState.Error -> {
                Spacer(modifier = Modifier.height(10.dp))
                AiFoodErrorCard(message = photoAnalysisState.message, onRetry = onRetryAnalysis)
            }
            else -> Unit
        }
    }
}

@Composable
private fun PhotoCaptureCard(
    title: String,
    icon: ImageVector,
    capturedPhoto: Bitmap?,
    isAnalyzing: Boolean,
    statusText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), color = CoralPrimary)
            } else if (capturedPhoto != null) {
                Image(
                    bitmap = capturedPhoto.asImageBitmap(),
                    contentDescription = title,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            if (statusText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AiFoodSuggestionCard(candidates: List<String>, onCandidateSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PositiveTagBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI 분석 결과, 이 음식으로 추정돼요",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PositiveTagText
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                candidates.forEach { candidate ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardWhite,
                        modifier = Modifier.clickable { onCandidateSelected(candidate) }
                    ) {
                        Text(
                            text = candidate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PositiveTagText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiFoodErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = WarningTagBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI 분석에 실패했어요",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = WarningTagText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = CardWhite,
                modifier = Modifier.clickable(onClick = onRetry)
            ) {
                Text(
                    text = "다시 시도",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarningTagText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<FoodMenuItem>,
    onFoodSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "메뉴 검색으로 기록하기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "음식 이름을 검색해보세요") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            results.forEach { food ->
                FoodMenuRow(name = food.name, onClick = { onFoodSelected(food.name) })
            }
            if (query.isNotBlank()) {
                AddCustomFoodRow(query = query, onClick = { onFoodSelected(query) })
            }
        }
    }
}

@Composable
private fun FoodMenuRow(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun AddCustomFoodRow(query: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = CoralPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "'$query' 메뉴가 없나요? 직접 추가하기",
                style = MaterialTheme.typography.bodyMedium,
                color = CoralPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ServingAmountSection(
    foodName: String,
    servingCount: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRegister: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = foodName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "섭취량 선택", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrease) {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "섭취량 줄이기")
                }
                Text(
                    text = "${servingCount}인분",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = onIncrease) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "섭취량 늘리기")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
            ) {
                Text(text = "식단 등록", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RegisteredMealsSection(meals: List<MealLogEntry>) {
    Column {
        Text(
            text = "오늘 등록한 식사",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            meals.sortedByDescending { it.eatenAt }.forEach { meal -> RegisteredMealRow(meal) }
        }
    }
}

@Composable
private fun RegisteredMealRow(meal: MealLogEntry) {
    val score = SkinScoreCalculator.scoreForMeals(meal.items)
    val (tagBackground, tagText, tagLabel) = when {
        score >= 80 -> Triple(PositiveTagBackground, PositiveTagText, "양호")
        score >= 60 -> Triple(CautionTagBackground, CautionTagText, "보통")
        else -> Triple(WarningTagBackground, WarningTagText, "주의")
    }
    val name = meal.items.joinToString(", ") { it.food.name }
    val servingSummary = meal.items.joinToString(", ") { "${it.portionRatio.let { ratio -> if (ratio == ratio.toInt().toDouble()) ratio.toInt().toString() else ratio.toString() }}인분" }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$name · $servingSummary",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = meal.eatenAt.toLocalTime().format(timeFormatter), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Surface(shape = RoundedCornerShape(50), color = tagBackground) {
                Text(
                    text = "${score}점 · $tagLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = tagText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
