package com.example.lionideaton.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
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
    }

    fun submit() {
        if (isSignUpMode) {
            when {
                nickname.isBlank() || email.isBlank() || password.isBlank() -> errorMessage = "모든 항목을 입력해주세요"
                password != passwordConfirm -> errorMessage = "비밀번호가 일치하지 않아요"
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Skin Basket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "먹은 만큼 예뻐지는 맞춤 피부 케어를 시작해보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = if (isSignUpMode) "회원가입" else "로그인",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "닉네임") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "이메일") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "비밀번호") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp)
                )
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "비밀번호 확인") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                errorMessage?.let { message ->
                    Text(text = message, style = MaterialTheme.typography.bodySmall, color = WarningTagText)
                }
                Button(
                    onClick = ::submit,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(text = if (isSignUpMode) "회원가입" else "로그인", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUpMode) "이미 계정이 있으신가요? " else "계정이 없으신가요? ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = if (isSignUpMode) "로그인" else "회원가입",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = CoralPrimary,
                modifier = Modifier.clickable { switchMode(!isSignUpMode) }
            )
        }
    }
}
