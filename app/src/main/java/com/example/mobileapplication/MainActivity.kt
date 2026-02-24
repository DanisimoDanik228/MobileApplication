package com.example.mobileapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Нужный импорт
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.content.Context
import android.content.Intent


class MainActivity : ComponentActivity() {

    // Это важно для применения языка при старте
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Screen.Main.route
            ) {
                composable(Screen.Main.route) { MainScreen(navController) }

                // Передаем функцию смены языка в экран настроек
                composable(Screen.Settings.route) {
                    SettingsScreen(navController) { lang ->
                        changeLanguage(lang)
                    }
                }

                composable(
                    route = Screen.Details.route,
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("itemId")
                    DetailsScreen(id, navController)
                }
            }
        }
    }

    // Функция для смены языка и перезагрузки Activity
    private fun changeLanguage(lang: String) {
        LocaleHelper.setLocale(this, lang)
        // Перезапускаем Activity, чтобы изменения вступили в силу
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}