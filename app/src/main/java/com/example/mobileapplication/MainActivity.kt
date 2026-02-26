package com.example.mobileapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobileapplication.Repository.LocalBookRepositoryImpl
// Импортируйте вашу тему (название может отличаться, проверьте в ui.theme/Theme.kt)
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        setContent {
            val savedTheme = remember {
                mutableIntStateOf(sharedPreferences.getInt("theme_mode", 0))
            }

            val darkTheme = when (savedTheme.intValue) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            MobileApplicationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val repository = LocalBookRepositoryImpl(applicationContext);
                NavHost(
                    navController = navController,
                    startDestination = Screen.Main.route
                ) {
                    composable(Screen.Main.route) {
                        MainScreen(navController,repository)
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            navController = navController,
                            onLanguageChange = { lang -> changeLanguage(lang) },
                            currentTheme = savedTheme.intValue,
                            onThemeChange = { newTheme ->
                                sharedPreferences.edit().putInt("theme_mode", newTheme).apply()
                                 savedTheme.intValue = newTheme
                            }
                        )
                    }

                    composable(
                        route = Screen.Details.route,
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) {
                        backStackEntry ->
                        val id = backStackEntry.arguments?.getString("itemId")
                        DetailsScreen(id, navController,repository)
                    }

                    composable(Screen.AddUser.route) {
                        AddUserScreen(navController, repository)
                    }
                }
            }
        }
    }

    private fun changeLanguage(lang: String) {
        LocaleHelper.setLocale(this, lang)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}