package com.example.lionideaton.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.SkinLogEntry
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

private val skinLogDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

@Composable
fun SkinLogScreen(modifier: Modifier = Modifier, skinLogViewModel: SkinLogViewModel = viewModel()) {
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var troubleLevel by remember { mutableFloatStateOf(3f) }
    var oilLevel by remember { mutableFloatStateOf(3f) }
    var drynessLevel by remember { mutableFloatStateOf(3f) }
    var memo by remember { mutableStateOf("") }

    val entries by skinLogViewModel.entries.collectAsState()

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) photo = bitmap
    }

    fun saveEntry() {
        skinLogViewModel.addEntry(
            photo = photo,
            troubleLevel = troubleLevel.toInt(),
            oilLevel = oilLevel.toInt(),
            drynessLevel = drynessLevel.toInt(),
            memo = memo
        )
        photo = null
        troubleLevel = 3f
        oilLevel = 3f
        drynessLevel = 3f
        memo = ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "오늘의 피부 기록",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "같은 장소·비슷한 조명·정면·필터 없이 촬영하면 비교가 더 정확해요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        SkinPhotoCaptureCard(photo = photo, onCapture = { photoLauncher.launch(null) })

        SelfAssessmentCard(
            troubleLevel = troubleLevel,
            onTroubleChange = { troubleLevel = it },
            oilLevel = oilLevel,
            onOilChange = { oilLevel = it },
            drynessLevel = drynessLevel,
            onDrynessChange = { drynessLevel = it },
            memo = memo,
            onMemoChange = { memo = it },
            onSave = ::saveEntry
        )

        if (entries.isNotEmpty()) {
            SkinLogHistorySection(entries = entries)
        }
    }
}

@Composable
private fun SkinPhotoCaptureCard(photo: Bitmap?, onCapture: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCapture),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (photo != null) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "촬영된 피부 사진",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "다시 촬영하려면 눌러주세요", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            } else {
                Surface(shape = CircleShape, color = SurfaceMuted) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "피부 사진 촬영하기", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun SelfAssessmentCard(
    troubleLevel: Float,
    onTroubleChange: (Float) -> Unit,
    oilLevel: Float,
    onOilChange: (Float) -> Unit,
    drynessLevel: Float,
    onDrynessChange: (Float) -> Unit,
    memo: String,
    onMemoChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오늘 피부 상태는 어땠나요?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            SelfAssessmentSlider(label = "트러블", value = troubleLevel, onValueChange = onTroubleChange)
            Spacer(modifier = Modifier.height(12.dp))
            SelfAssessmentSlider(label = "유분", value = oilLevel, onValueChange = onOilChange)
            Spacer(modifier = Modifier.height(12.dp))
            SelfAssessmentSlider(label = "건조함", value = drynessLevel, onValueChange = onDrynessChange)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "메모 (선택)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
            ) {
                Text(text = "피부 기록 저장", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SelfAssessmentSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = "${value.toInt()} / 5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CoralPrimary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(thumbColor = CoralPrimary, activeTrackColor = CoralPrimary)
        )
    }
}

@Composable
private fun SkinLogHistorySection(entries: List<SkinLogEntry>) {
    Column {
        Text(
            text = "피부 기록 히스토리",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            entries.sortedByDescending { it.loggedAt }.forEach { entry -> SkinLogHistoryRow(entry) }
        }
    }
}

@Composable
private fun SkinLogHistoryRow(entry: SkinLogEntry) {
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
            if (entry.photo != null) {
                Image(
                    bitmap = entry.photo.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(10.dp), color = SurfaceMuted) {}
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.loggedAt.format(skinLogDateFormatter),
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
            }
        }
    }
}
