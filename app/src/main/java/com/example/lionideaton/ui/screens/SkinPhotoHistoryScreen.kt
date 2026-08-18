package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.SkinLogEntry
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private const val HISTORY_LIMIT = 10
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

// Simple recent-entries list — not paginated, matches the meal log history screen's scope.
@Composable
fun SkinPhotoHistoryScreen(modifier: Modifier = Modifier, skinLogViewModel: SkinLogViewModel = viewModel()) {
    val entries by skinLogViewModel.entries.collectAsState()
    val recent = entries.sortedByDescending { it.loggedAt }.take(HISTORY_LIMIT)

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
                text = "아직 기록된 피부 사진이 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            recent.forEach { entry -> SkinPhotoHistoryRow(entry) }
        }
    }
}

@Composable
private fun SkinPhotoHistoryRow(entry: SkinLogEntry) {
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
            SkinPhotoThumbnail(photo = entry.photo)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.loggedAt.format(dateTimeFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "트러블 ${entry.troubleLevel} · 유분 ${entry.oilLevel} · 건조함 ${entry.drynessLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                entry.memo?.let { memo ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = memo, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SkinPhotoThumbnail(photo: Bitmap?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(color = SurfaceMuted, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(imageVector = Icons.Filled.Face, contentDescription = null, tint = TextSecondary)
        }
    }
}
