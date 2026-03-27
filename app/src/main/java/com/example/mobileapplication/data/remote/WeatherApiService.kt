package com.example.mobileapplication.data.remote

import com.example.mobileapplication.core.Constants
import com.example.mobileapplication.domain.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String = Constants.CITY_WEATHER,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Constants.WEATHER_API_KEY
    ): WeatherResponse
}