package com.example.lionideaton.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

// NetworkModule.authToken은 프로세스 메모리에만 있어 앱을 완전히 종료하면 사라진다.
// 여기 저장된 값을 UserProfileViewModel이 시작 시 읽어 다시 채워 넣어야 로그인이 유지된다.
class SessionStore(context: Context) {
    private val appContext = context.applicationContext

    val tokenFlow: Flow<String?> = appContext.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clearToken() {
        appContext.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
