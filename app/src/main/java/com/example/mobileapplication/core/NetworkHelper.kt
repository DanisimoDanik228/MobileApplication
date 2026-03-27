package com.example.mobileapplication.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

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

    suspend fun getPing(): String {
        return try {
            val start = System.currentTimeMillis()
            val address = java.net.InetAddress.getByName("8.8.8.8") // Или ваш ngrok URL
            if (address.isReachable(2000)) {
                val end = System.currentTimeMillis()
                "${end - start} ms"
            } else {
                "Timeout"
            }
        } catch (e: Exception) {
            "Error"
        }
    }
}