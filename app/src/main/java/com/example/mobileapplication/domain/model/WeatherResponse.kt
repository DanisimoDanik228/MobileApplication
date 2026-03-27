package com.example.mobileapplication.domain.model

data class WeatherResponse(
    val main: MainData,
    val name: String
)
data class MainData(
    val temp: Double // Температура
)