package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.data.model.SkinType
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onCompleted: () -> Unit = {}
) {
    var selectedSkinType by remember { mutableStateOf<SkinType?>(null) }
    var selectedConcerns by remember { mutableStateOf(setOf<SkinConcern>()) }
    var allergyInputs by remember { mutableStateOf(listOf("")) }
    var dislikeInputs by remember { mutableStateOf(listOf("")) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text(text = "피부 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "맞춤 서비스를 이용하려면 반드시 선택해 주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "피부타입", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "(1개)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkinType.entries.forEach { type ->
                    SelectableChip(
                        text = type.label,
                        selected = selectedSkinType == type,
                        onClick = { selectedSkinType = type }
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "피부고민", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkinConcern.entries.forEach { concern ->
                    SelectableChip(
                        text = concern.label,
                        selected = selectedConcerns.contains(concern),
                        onClick = {
                            selectedConcerns = when {
                                concern == SkinConcern.NONE -> setOf(SkinConcern.NONE)
                                selectedConcerns.contains(concern) -> selectedConcerns - concern
                                else -> (selectedConcerns + concern) - SkinConcern.NONE
                            }
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

        ChipInputCard(
            title = "알레르기가 있는 재료",
            placeholder = "예: 밀가루",
            inputs = allergyInputs,
            onInputsChange = { allergyInputs = it }
        )

        ChipInputCard(
            title = "못 먹거나 선호하지 않는 음식",
            placeholder = "예: 오이",
            inputs = dislikeInputs,
            onInputsChange = { dislikeInputs = it }
        )

        Button(
            onClick = {
                val skinType = selectedSkinType ?: return@Button
                userProfileViewModel.completeOnboarding(
                    skinType = skinType,
                    concerns = selectedConcerns.toList(),
                    allergies = allergyInputs.map { it.trim() }.filter { it.isNotBlank() },
                    dislikes = dislikeInputs.map { it.trim() }.filter { it.isNotBlank() }
                )
                onCompleted()
            },
            enabled = selectedSkinType != null && selectedConcerns.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
        ) {
            Text(text = "시작하기", fontWeight = FontWeight.Bold)
        }
        }
    }
}

@Composable
private fun OnboardingCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// Each "+" tap appends one more blank input row rather than reusing a single field —
// the user can fill in several items at once, each in its own removable row.
@Composable
private fun ChipInputCard(
    title: String,
    placeholder: String,
    inputs: List<String>,
    onInputsChange: (List<String>) -> Unit
) {
    OnboardingCard(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            inputs.forEachIndexed { index, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            onInputsChange(inputs.toMutableList().also { it[index] = newValue })
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(text = placeholder) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        val updated = inputs.toMutableList().also { it.removeAt(index) }
                        onInputsChange(updated.ifEmpty { listOf("") })
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "삭제", tint = TextSecondary)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onInputsChange(inputs + "") }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "입력칸 추가", style = MaterialTheme.typography.bodySmall, color = CoralPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
