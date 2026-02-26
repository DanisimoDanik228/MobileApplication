package com.example.mobileapplication

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, language: String) {
        persist(context, language)
        updateResources(context, language)
    }

    fun onAttach(context: Context): Context? {
        val lang = getPersistedData(context, Locale.getDefault().getLanguage())
        return LocaleHelper.updateResources(context, lang!!)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String?): String? {
        val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    private fun persist(context: Context, language: String?) {
        val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    private fun updateResources(context: Context, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.getResources()
        val config = Configuration(res.getConfiguration())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.getDisplayMetrics())
            return context
        }
    }
}