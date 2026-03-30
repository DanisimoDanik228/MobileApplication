package com.example.mobileapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.mobileapplication.core.LocaleHelper
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.data.local.AppDatabase
import com.example.mobileapplication.data.remote.RetrofitClient
import com.example.mobileapplication.data.repository.RoomBookRepository
import com.example.mobileapplication.data.repository.RemoteBookRepository
import com.example.mobileapplication.ui.screens.AddUserScreen
import com.example.mobileapplication.ui.screens.DetailsScreen
import com.example.mobileapplication.ui.screens.MainScreen
import com.example.mobileapplication.ui.screens.Screen
import com.example.mobileapplication.ui.screens.SettingsScreen
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.viewmodels.BookViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        // Применяем локализацию при запуске
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Установка Splash Screen (перед super.onCreate)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 2. Запрос разрешений на уведомления для Android 13+
        checkNotificationPermission()

        // 3. Инициализация системных помощников
        val networkHelper = NetworkHelper(applicationContext)
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        // 4. Инициализация базы данных и репозиториев
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "books"
        )
            .fallbackToDestructiveMigration()
            // .allowMainThreadQueries() // Лучше не использовать, так как у нас есть корутины
            .build()

        val apiService = RetrofitClient.apiService
        val roomRepo = RoomBookRepository(db.bookDao())
        val remoteRepo = RemoteBookRepository(apiService, networkHelper)

        // 5. Инициализация ViewModel
        val viewModel = BookViewModel(
            networkHelper,
            remoteRepo,
            roomRepo,
            sharedPreferences)

        setContent {
            // Подписка на состояния из ViewModel
            val isRemoteMode by viewModel.isRemoteMode.collectAsState()

            // Работа с темой
            val savedThemeMode = remember {
                mutableIntStateOf(sharedPreferences.getInt("theme_mode", 0))
            }

            val isDarkTheme = when (savedThemeMode.intValue) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System
            }

            MobileApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Main.route
                ) {
                    // ГЛАВНЫЙ ЭКРАН
                    composable(Screen.Main.route) {
                        MainScreen(navController, viewModel)
                    }

                    // ЭКРАН НАСТРОЕК
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            navController = navController,
                            viewModel = viewModel, // Передаем viewModel для уведомлений
                            onLanguageChange = { lang -> changeLanguage(lang) },
                            currentTheme = savedThemeMode.intValue,
                            onThemeChange = { newTheme ->
                                sharedPreferences.edit().putInt("theme_mode", newTheme).apply()
                                savedThemeMode.intValue = newTheme
                            },
                            isRemoteMode = isRemoteMode,
                            onRepositoryModeChange = { viewModel.setRepositoryMode(it) }
                        )
                    }

                    // ЭКРАН ДЕТАЛЕЙ
                    composable(
                        route = Screen.Details.route,
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val idString = backStackEntry.arguments?.getString("itemId")
                        val id = idString?.toIntOrNull() ?: 0
                        DetailsScreen(id, navController, viewModel)
                    }

                    // ЭКРАН ДОБАВЛЕНИЯ
                    composable(Screen.AddUser.route) {
                        AddUserScreen(navController, viewModel)
                    }
                }
            }
        }
    }

    // Вспомогательный метод для смены языка
    private fun changeLanguage(lang: String) {
        LocaleHelper.setLocale(this, lang)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    // Запрос разрешений на уведомления
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101
                )
            }
        }
    }
}