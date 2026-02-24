package com.example.mobileapplication

import android.content.Context
import android.content.SharedPreferences

class ThemeSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Сохраняем выбор: 0 - системная, 1 - светлая, 2 - темная
    fun saveTheme(themeIndex: Int) {
        prefs.edit().putInt("theme_mode", themeIndex).apply()
    }

    fun getTheme(): Int {
        return prefs.getInt("theme_mode", 0) // По умолчанию системная (0)
    }
}