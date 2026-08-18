package com.example.lionideaton.ui.screens

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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private const val HISTORY_LIMIT = 10
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

// Simple recent-entries list — not paginated, matches the skin photo history screen's scope.
@Composable
fun MealLogHistoryScreen(modifier: Modifier = Modifier, mealLogViewModel: MealLogViewModel = viewModel()) {
    val entries by mealLogViewModel.entries.collectAsState()
    val recent = entries.sortedByDescending { it.eatenAt }.take(HISTORY_LIMIT)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "최근 ${recent.size}건",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        if (recent.isEmpty()) {
            Text(
                text = "아직 기록된 식사가 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            recent.forEach { entry -> MealLogHistoryRow(entry) }
        }
    }
}

@Composable
private fun MealLogHistoryRow(entry: MealLogEntry) {
    val foodNames = entry.items.joinToString(", ") { it.food.name }
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
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), color = SurfaceMuted) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Restaurant, contentDescription = null, tint = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodNames.ifBlank { "기록된 음식 없음" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.mealType.label} · ${entry.eatenAt.format(dateTimeFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
