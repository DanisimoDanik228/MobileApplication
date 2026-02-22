package com.example.mobileapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Создаем контроллер навигации
            val navController = rememberNavController()

            // Настраиваем маршруты
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route
            ) {
                // Главная
                composable(Screen.Main.route) {
                    MainScreen(navController)
                }

                // Настройки
                composable(Screen.Settings.route) {
                    SettingsScreen(navController)
                }

                // Детали (с аргументом)
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
}