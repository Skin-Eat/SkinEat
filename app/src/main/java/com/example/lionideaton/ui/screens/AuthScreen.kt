package com.example.lionideaton.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.CreamBackground
import com.example.lionideaton.ui.theme.PeachSecondary
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import com.example.lionideaton.ui.theme.WarningTagText

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onAuthenticated: () -> Unit = {}
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authState by userProfileViewModel.authState.collectAsState()
    val isSubmitting = authState is AuthUiState.Loading

    // signUp/login은 백엔드 호출이라 결과가 바로 안 옴 — authState가 Success/Error로
    // 바뀌는 걸 여기서 지켜보다가 화면 전환/에러 표시를 하고, 처리 후엔 다시 Idle로 돌려놓는다
    // (안 그러면 다음 시도 때 이전 Success/Error가 남아있어서 오작동함).
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                userProfileViewModel.resetAuthState()
                onAuthenticated()
            }
            is AuthUiState.Error -> {
                errorMessage = state.message
                userProfileViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    fun switchMode(signUp: Boolean) {
        isSignUpMode = signUp
        errorMessage = null
        passwordVisible = false
        passwordConfirmVisible = false
    }

    fun submit() {
        if (isSignUpMode) {
            when {
                nickname.isBlank() || email.isBlank() || password.isBlank() ->
                    errorMessage = "모든 항목을 입력해주세요"
                password != passwordConfirm ->
                    errorMessage = "비밀번호가 일치하지 않아요"
                else -> {
                    errorMessage = null
                    userProfileViewModel.signUp(email.trim(), password, nickname.trim())
                }
            }
        } else {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "이메일과 비밀번호를 입력해주세요"
            } else {
                errorMessage = null
                userProfileViewModel.login(email.trim(), password)
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CoralPrimary,
        unfocusedBorderColor = Color(0xFFE8E8E8),
        focusedContainerColor = CardWhite,
        unfocusedContainerColor = CardWhite,
        cursorColor = CoralPrimary,
        focusedLabelColor = CoralPrimary,
        unfocusedLabelColor = TextSecondary
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PeachSecondary, CreamBackground, CreamBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SkinEat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSignUpMode) {
                    "피부와 식단을 연결하는\n첫 걸음을 시작해요"
                } else {
                    "다시 오신 걸 환영해요"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isSignUpMode) {
                    AuthTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = "닉네임",
                        leadingIcon = Icons.Filled.Person,
                        colors = fieldColors
                    )
                }
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "이메일",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                    colors = fieldColors
                )
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "비밀번호",
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    colors = fieldColors
                )
                if (isSignUpMode) {
                    AuthTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = "비밀번호 확인",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        passwordVisible = passwordConfirmVisible,
                        onTogglePassword = { passwordConfirmVisible = !passwordConfirmVisible },
                        colors = fieldColors
                    )
                }
            }

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningTagText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = ::submit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = CoralPrimary.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "회원가입" else "로그인",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpMode) "이미 계정이 있으신가요? " else "계정이 없으신가요? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = if (isSignUpMode) "로그인" else "회원가입",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary,
                    modifier = Modifier.clickable { switchMode(!isSignUpMode) }
                )
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    colors: androidx.compose.material3.TextFieldColors,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = colors,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = TextSecondary
                    )
                }
            }
        } else {
            null
        }
    )
}
