package com.example.lionideaton.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lionideaton.data.RecipeSeedData
import com.example.lionideaton.data.model.RecipeDetail
import com.example.lionideaton.data.model.SkinBenefit
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PositiveTagBackground
import com.example.lionideaton.ui.theme.PositiveTagText
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

@Composable
fun RecipeScreen(modifier: Modifier = Modifier) {
    val recipe = RecipeSeedData.featuredRecipe

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "조리시간 ${recipe.cookingTimeMinutes}분 · ${recipe.servings}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        IngredientsCard(ingredients = recipe.ingredients)

        StepsCard(steps = recipe.steps)

        SkinBenefitsCard(benefits = recipe.skinBenefits)
    }
}

@Composable
private fun IngredientsCard(ingredients: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "재료",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ingredients.forEach { ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = CoralPrimary,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = ingredient, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsCard(steps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "조리 순서",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                steps.forEachIndexed { index, step ->
                    Row {
                        Surface(shape = RoundedCornerShape(50), color = SurfaceMuted) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = step, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinBenefitsCard(benefits: List<SkinBenefit>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PositiveTagBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "이 레시피의 피부 효과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PositiveTagText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                benefits.forEach { benefit ->
                    Column {
                        Text(
                            text = benefit.nutrient,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PositiveTagText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = benefit.description, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "실제 효과는 개인차가 있으며, 일반적인 영양 정보를 바탕으로 한 참고용 설명이에요. 의학적 효능을 보장하지 않아요.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
