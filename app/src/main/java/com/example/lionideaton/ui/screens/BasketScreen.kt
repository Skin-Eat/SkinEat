package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.IngredientSeedData
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.domain.SkinScoreCalculator
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PeachSecondary
import com.example.lionideaton.ui.theme.PhotoPromptAccent
import com.example.lionideaton.ui.theme.PhotoPromptBackground
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

private data class RecommendationGroup(val keyNutrient: String, val options: List<Ingredient>)

private fun optionsFor(keyNutrient: String): List<Ingredient> =
    listOfNotNull(IngredientSeedData.primaryFor(keyNutrient)) + IngredientSeedData.subsFor(keyNutrient)

private fun nutrientLabel(keyNutrient: String): String = when (keyNutrient) {
    IngredientSeedData.KEY_OMEGA3 -> "오메가3"
    IngredientSeedData.KEY_VIT_C -> "비타민C"
    IngredientSeedData.KEY_VIT_E -> "비타민E"
    IngredientSeedData.KEY_ZINC -> "아연"
    else -> keyNutrient
}

private fun iconFor(keyNutrient: String): ImageVector = IngredientSeedData.iconFor(keyNutrient)

// Loose mapping onto the app's 4 tracked nutrient axes — mirrors SkinScoreCalculator's doc
// comment (글당부하/항염/장벽·수분/항산화), not a clinical claim about any single concern.
private fun nutrientKeysFor(concern: SkinConcern): List<String> = when (concern) {
    SkinConcern.ATOPY -> listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_E)
    SkinConcern.ACNE -> listOf(IngredientSeedData.KEY_ZINC, IngredientSeedData.KEY_OMEGA3)
    SkinConcern.SENSITIVE -> listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_E)
    SkinConcern.PIGMENTATION -> listOf(IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E)
    SkinConcern.SEBUM -> listOf(IngredientSeedData.KEY_ZINC, IngredientSeedData.KEY_VIT_C)
    SkinConcern.DARK_CIRCLE -> listOf(IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E)
    SkinConcern.INNER_DRYNESS -> listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_E)
    SkinConcern.WRINKLE -> listOf(IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E)
    SkinConcern.PORES -> listOf(IngredientSeedData.KEY_ZINC, IngredientSeedData.KEY_VIT_C)
    SkinConcern.REDNESS -> listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_C)
    SkinConcern.KERATIN -> listOf(IngredientSeedData.KEY_VIT_E, IngredientSeedData.KEY_OMEGA3)
    SkinConcern.NONE -> emptyList()
}

private fun concernsAddressedBy(keyNutrient: String, userConcerns: List<SkinConcern>): List<SkinConcern> =
    userConcerns.filter { concern -> keyNutrient in nutrientKeysFor(concern) }

private data class RecommendedRecipe(val name: String, val cookingTime: String, val barrierScore: String)

private val recommendedRecipe = RecommendedRecipe("연어 브로콜리 지중해식 포케", "15분", "94%")

@Composable
fun BasketScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel,
    mealLogViewModel: MealLogViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onRecipeClick: () -> Unit = {}
) {
    val allEntries by mealLogViewModel.entries.collectAsState()
    val recentEntries = remember(allEntries) { mealLogViewModel.recentEntries() }
    val deficiency = remember(recentEntries) { SkinScoreCalculator.analyzeDeficiency(recentEntries) }
    val profile by userProfileViewModel.profile.collectAsState()
    val userConcerns = profile.concerns

    val deficientKeys = remember(deficiency) {
        buildList {
            if (deficiency.omega3Deficient) add(IngredientSeedData.KEY_OMEGA3)
            if (deficiency.vitCDeficient) add(IngredientSeedData.KEY_VIT_C)
            if (deficiency.vitEDeficient) add(IngredientSeedData.KEY_VIT_E)
            if (deficiency.zincDeficient) add(IngredientSeedData.KEY_ZINC)
        }
    }
    // Nothing deficient? Fall back to all 4 axes so the screen still has something to show.
    val activeKeys = deficientKeys.ifEmpty {
        listOf(IngredientSeedData.KEY_OMEGA3, IngredientSeedData.KEY_VIT_C, IngredientSeedData.KEY_VIT_E, IngredientSeedData.KEY_ZINC)
    }
    // Concern-matched axes float to the top so the feed leads with the most personalized picks.
    val sortedKeys = remember(activeKeys, userConcerns) {
        activeKeys.sortedByDescending { key -> concernsAddressedBy(key, userConcerns).size }
    }
    val groups = remember(sortedKeys) { sortedKeys.map { key -> RecommendationGroup(key, optionsFor(key)) } }
    var groupIndices by remember(groups) { mutableStateOf(List(groups.size) { 0 }) }

    val cartItems by cartViewModel.items.collectAsState()

    val headline = if (userConcerns.isNotEmpty()) {
        val concernLabels = userConcerns.joinToString("·") { it.label }
        "${profile.nickname.ifBlank { "회원" }}님의 ${concernLabels} 고민에 맞춘 오늘의 추천"
    } else {
        "내일의 장바구니 리스트"
    }
    val subtitle = if (deficientKeys.isNotEmpty()) {
        "최근 7일간 부족한 영양을 채우기 위해 골라본 식재료예요."
    } else {
        "최근 7일간 식습관이 균형 잡혀 있어요. 꾸준히 챙기면 좋은 식재료예요."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ProBadge()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        PremiumFeedBanner()

        groups.forEachIndexed { index, group ->
            val ingredient = group.options[groupIndices[index]]
            val isDeficient = deficientKeys.contains(group.keyNutrient)
            val matchedConcerns = concernsAddressedBy(group.keyNutrient, userConcerns)
            IngredientRecommendationCard(
                ingredient = ingredient,
                keyNutrient = group.keyNutrient,
                isDeficient = isDeficient,
                matchedConcerns = matchedConcerns,
                isInBasket = cartItems.any { it.name == ingredient.name },
                onShowAlternative = {
                    groupIndices = groupIndices.toMutableList().apply {
                        this[index] = (this[index] + 1) % group.options.size
                    }
                },
                onAddToBasket = {
                    cartViewModel.addItem(ingredient, group.keyNutrient)
                }
            )
        }

        RecommendedRecipeSection(recipe = recommendedRecipe, onClick = onRecipeClick)
    }
}

@Composable
private fun ProBadge() {
    Surface(shape = RoundedCornerShape(50), color = PeachSecondary) {
        Text(
            text = "PRO",
            style = MaterialTheme.typography.labelSmall,
            color = CoralPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// No paywall lock yet (explicitly deferred by the team) — this just signals it's the premium tier.
@Composable
private fun PremiumFeedBanner() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = PeachSecondary) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⭐", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "유료 회원 전용 추천 피드예요. 지금은 잠금 없이 미리 체험할 수 있어요.",
                style = MaterialTheme.typography.labelSmall,
                color = CoralPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IngredientRecommendationCard(
    ingredient: Ingredient,
    keyNutrient: String,
    isDeficient: Boolean,
    matchedConcerns: List<SkinConcern>,
    isInBasket: Boolean,
    onShowAlternative: () -> Unit,
    onAddToBasket: () -> Unit
) {
    val concernLabel = matchedConcerns.joinToString("·") { it.label }
    val reasonText = when {
        matchedConcerns.isNotEmpty() && isDeficient ->
            "${concernLabel} 고민이 있으시고 최근 ${nutrientLabel(keyNutrient)} 섭취도 부족해서 추천돼요"
        matchedConcerns.isNotEmpty() ->
            "${concernLabel} 고민에 도움이 될 수 있는 ${nutrientLabel(keyNutrient)} 재료예요"
        isDeficient -> "최근 7일간 ${nutrientLabel(keyNutrient)} 섭취가 부족해서 추천돼요"
        else -> "${nutrientLabel(keyNutrient)} 보충에 꾸준히 도움이 되는 재료예요"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            IngredientHeroImage(icon = iconFor(keyNutrient))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (matchedConcerns.isNotEmpty()) {
                        ConcernBadge(text = concernLabel)
                    }
                    NutrientBadge(text = nutrientLabel(keyNutrient))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = reasonText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            ingredient.appealNote?.let { note ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onShowAlternative,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(text = "다른 재료 보기")
                }
                Button(
                    onClick = onAddToBasket,
                    enabled = !isInBasket,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
                ) {
                    Text(text = if (isInBasket) "담았어요" else "장바구니 담기", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IngredientHeroImage(icon: ImageVector) {
    // Placeholder illustration until real ingredient photos/an image pipeline are wired up.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceMuted
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun NutrientBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = PhotoPromptBackground) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PhotoPromptAccent,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// Distinct from NutrientBadge so it's visually clear a card is recommended for two different reasons.
@Composable
private fun ConcernBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = PeachSecondary) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CoralPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RecommendedRecipeSection(recipe: RecommendedRecipe, onClick: () -> Unit) {
    Column {
        Text(
            text = "추천 피부 구원 레시피",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = PositiveTagBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.SetMeal,
                            contentDescription = null,
                            tint = PositiveTagText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "조리시간 ${recipe.cookingTime} | 장벽 강화도 ${recipe.barrierScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}


