package com.example.mobileapplication.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NetworkHelper(private val context: Context) {

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    // Внутри NetworkHelper
    suspend fun getPing(): String {
        return try {
            // Очищаем URL от https:// и лишних слешей для надежности,
            // но для OkHttp лучше использовать полный URL
            val url = Constants.CLOUDFLARETUNNEL_URL

            val client = OkHttpClient.Builder()
                .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.MILLISECONDS) // Ждем не больше 2 сек
                .build()

            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val start = System.currentTimeMillis()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 404) { // 404 тоже значит, что сервер ответил
                    val end = System.currentTimeMillis()
                    "${end - start} ms"
                } else {
                    "Error ${response.code}"
                }
            }
        } catch (e: Exception) {
            "Timeout"
        }
    }
}