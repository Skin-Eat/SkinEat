package com.example.lionideaton.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lionideaton.data.model.ConstraintType
import com.example.lionideaton.data.model.SkinConcern
import com.example.lionideaton.data.model.UserProfile
import com.example.lionideaton.notification.ReminderScheduler
import com.example.lionideaton.notification.ReminderType
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.PhotoPromptAccent
import com.example.lionideaton.ui.theme.PhotoPromptBackground
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagBackground
import com.example.lionideaton.ui.theme.WarningTagText

private data class SkinTargetTag(val label: String, val isWarning: Boolean)

private fun tagsFor(profile: UserProfile): List<SkinTargetTag> = buildList {
    profile.concerns.forEach { concern -> add(SkinTargetTag("${concern.label} 집중 케어", isWarning = false)) }
    profile.constraints.filter { it.type == ConstraintType.DISLIKE }.forEach {
        add(SkinTargetTag("비선호: ${it.ingredientName}", isWarning = false))
    }
    profile.constraints.filter { it.type == ConstraintType.ALLERGY }.forEach {
        add(SkinTargetTag("알레르기: ${it.ingredientName} 피하기", isWarning = true))
    }
}

private data class MyPageListItem(val title: String, val trailingInfo: String, val onClick: () -> Unit)

@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel,
    skinLogViewModel: SkinLogViewModel,
    userProfileViewModel: UserProfileViewModel,
    mealLogViewModel: MealLogViewModel,
    onSkinPhotoHistoryClick: () -> Unit = {},
    onMealLogHistoryClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val profile by userProfileViewModel.profile.collectAsState()
    val cartItems by cartViewModel.items.collectAsState()
    val skinLogEntries by skinLogViewModel.entries.collectAsState()
    val skinPhotoCount = skinLogEntries.count { it.photo != null }
    val mealLogEntries by mealLogViewModel.entries.collectAsState()
    var mealLogReminderEnabled by remember { mutableStateOf(true) }
    var skinPhotoReminderEnabled by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun setReminder(type: ReminderType, enabled: Boolean) {
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            ReminderScheduler.schedule(context, type)
        } else {
            ReminderScheduler.cancel(context, type)
        }
    }

    // Sync WorkManager to the initial toggle state once, without prompting for permission —
    // the prompt should only appear from an explicit user action (the switch onCheckedChange below).
    LaunchedEffect(Unit) {
        if (mealLogReminderEnabled) ReminderScheduler.schedule(context, ReminderType.MEAL_LOG)
        if (skinPhotoReminderEnabled) ReminderScheduler.schedule(context, ReminderType.SKIN_PHOTO)
    }

    val myPageListItems = listOf(
        MyPageListItem("피부 사진 히스토리", "${skinPhotoCount}장 보관 중", onSkinPhotoHistoryClick),
        MyPageListItem("식습관 로그 전체 보기", "총 ${mealLogEntries.size}식단", onMealLogHistoryClick),
        MyPageListItem("구독 & 멤버십 관리", "유료 등급 사용 중", {})
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileHeader(profile = profile)

        SkinTargetCard(
            profile = profile,
            onSave = { concerns, allergies, dislikes ->
                userProfileViewModel.updateSkinTargets(concerns, allergies, dislikes)
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MyStatCard(label = "피부 분석 횟수", value = "14회", modifier = Modifier.weight(1f))
            MyStatCard(label = "식단 기재 일수", value = "28일", modifier = Modifier.weight(1f))
            MyStatCard(label = "바스켓 담은 수", value = "${cartItems.size}개", modifier = Modifier.weight(1f))
        }

        NotificationSettingsCard(
            mealLogReminderEnabled = mealLogReminderEnabled,
            onMealLogReminderChange = {
                mealLogReminderEnabled = it
                setReminder(ReminderType.MEAL_LOG, it)
            },
            skinPhotoReminderEnabled = skinPhotoReminderEnabled,
            onSkinPhotoReminderChange = {
                skinPhotoReminderEnabled = it
                setReminder(ReminderType.SKIN_PHOTO, it)
            }
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            myPageListItems.forEach { item -> MyPageListRow(item) }
        }

        LogoutButton(onClick = onLogout)
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Text(text = "로그아웃", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileHeader(profile: UserProfile) {
    val subtitle = buildString {
        append(profile.skinType?.let { "${it.label} 피부" } ?: "피부타입 미설정")
        if (profile.concerns.isNotEmpty()) {
            append(" · ")
            append(profile.concerns.joinToString(", ") { it.label })
            append(" 타겟 중")
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = SurfaceMuted) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${profile.nickname} 님", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = CoralPrimary) {
                    Text(
                        text = "PRO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SkinTargetCard(
    profile: UserProfile,
    onSave: (concerns: List<SkinConcern>, allergies: List<String>, dislikes: List<String>) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

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
                    text = "나의 피부 분석 타겟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (!isEditing) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "편집", tint = TextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            if (isEditing) {
                SkinTargetEditForm(
                    profile = profile,
                    onCancel = { isEditing = false },
                    onSave = { concerns, allergies, dislikes ->
                        onSave(concerns, allergies, dislikes)
                        isEditing = false
                    }
                )
            } else {
                val tags = tagsFor(profile)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.filter { !it.isWarning }.forEach { tag ->
                        Surface(shape = RoundedCornerShape(50), color = PhotoPromptBackground) {
                            Text(
                                text = tag.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = PhotoPromptAccent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                tags.filter { it.isWarning }.forEach { tag ->
                    Surface(shape = RoundedCornerShape(50), color = WarningTagBackground) {
                        Text(
                            text = tag.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningTagText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkinTargetEditForm(
    profile: UserProfile,
    onCancel: () -> Unit,
    onSave: (concerns: List<SkinConcern>, allergies: List<String>, dislikes: List<String>) -> Unit
) {
    var editingConcerns by remember { mutableStateOf(profile.concerns.toSet()) }
    var allergyInput by remember { mutableStateOf("") }
    var allergies by remember {
        mutableStateOf(profile.constraints.filter { it.type == ConstraintType.ALLERGY }.map { it.ingredientName })
    }
    var dislikeInput by remember { mutableStateOf("") }
    var dislikes by remember {
        mutableStateOf(profile.constraints.filter { it.type == ConstraintType.DISLIKE }.map { it.ingredientName })
    }

    Column {
        Text(text = "피부 고민 (여러 개 가능)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkinConcern.entries.forEach { concern ->
                val selected = editingConcerns.contains(concern)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        editingConcerns = if (selected) editingConcerns - concern else editingConcerns + concern
                    }
                ) {
                    Text(
                        text = concern.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) Color.White else TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        EditableChipRow(
            label = "알레르기 재료",
            placeholder = "예: 밀가루",
            inputValue = allergyInput,
            onInputChange = { allergyInput = it },
            items = allergies,
            onAdd = {
                if (allergyInput.isNotBlank()) {
                    allergies = allergies + allergyInput.trim()
                    allergyInput = ""
                }
            },
            onRemove = { name -> allergies = allergies - name }
        )

        Spacer(modifier = Modifier.height(16.dp))
        EditableChipRow(
            label = "못 먹거나 선호하지 않는 음식",
            placeholder = "예: 오이",
            inputValue = dislikeInput,
            onInputChange = { dislikeInput = it },
            items = dislikes,
            onAdd = {
                if (dislikeInput.isNotBlank()) {
                    dislikes = dislikes + dislikeInput.trim()
                    dislikeInput = ""
                }
            },
            onRemove = { name -> dislikes = dislikes - name }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = "취소")
            }
            Button(
                onClick = { onSave(editingConcerns.toList(), allergies, dislikes) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
            ) {
                Text(text = "저장", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditableChipRow(
    label: String,
    placeholder: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    items: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = placeholder) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "추가", tint = CoralPrimary)
            }
        }
        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Surface(shape = RoundedCornerShape(50), color = PhotoPromptBackground) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
                            Text(text = item, style = MaterialTheme.typography.bodySmall, color = PhotoPromptAccent, fontWeight = FontWeight.Medium)
                            IconButton(onClick = { onRemove(item) }, modifier = Modifier.size(20.dp).padding(start = 2.dp)) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "삭제", tint = PhotoPromptAccent, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    mealLogReminderEnabled: Boolean,
    onMealLogReminderChange: (Boolean) -> Unit,
    skinPhotoReminderEnabled: Boolean,
    onSkinPhotoReminderChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "알림 설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationSettingRow(
                label = "식사 마친 시간 기록 알림",
                checked = mealLogReminderEnabled,
                onCheckedChange = onMealLogReminderChange
            )
            Spacer(modifier = Modifier.height(10.dp))
            NotificationSettingRow(
                label = "피부 사진 촬영 리마인더",
                checked = skinPhotoReminderEnabled,
                onCheckedChange = onSkinPhotoReminderChange
            )
        }
    }
}

@Composable
private fun NotificationSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = CoralPrimary, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun MyPageListRow(item: MyPageListItem) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.trailingInfo, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}
