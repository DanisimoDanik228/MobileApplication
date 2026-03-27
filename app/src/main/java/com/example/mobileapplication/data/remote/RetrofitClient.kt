package com.example.mobileapplication.data.remote

import com.example.mobileapplication.core.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true") // Чтобы ngrok не блокировал запрос
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: BookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.NGROK_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(BookApiService::class.java)
    }
}