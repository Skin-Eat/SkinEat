package com.example.lionideaton.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // 가비아 g클라우드에 배포된 백엔드. 도메인 없이 IP+HTTP로 테스트 중 — 나중에 도메인+HTTPS
    // 붙이면 이 주소를 바꿀 것 (deploy/DEPLOY.md 참고). 에뮬레이터에서 PC 로컬 서버로 접속하던
    // 이전 값(10.0.2.2)이나 집 Wi-Fi IP 값은 이제 안 씀 — 이 주소는 Wi-Fi/네트워크 상관없이 접속됨.
    private const val BASE_URL = "http://1.201.116.202:8000/"

    // 로그인/회원가입 응답의 accessToken을 여기 저장해두고, 이후 모든 요청에 자동으로
    // Authorization 헤더로 붙인다. 이 필드 자체는 프로세스 메모리에만 있지만, 영속 저장은
    // UserProfileViewModel이 SessionStore를 통해 앱 시작 시 여기로 복원해준다.
    @Volatile
    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authToken
        val request = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: SkinBasketApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkinBasketApi::class.java)
    }
}
