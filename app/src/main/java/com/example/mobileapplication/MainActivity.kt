package com.example.mobileapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.mobileapplication.core.FileHelper
import com.example.mobileapplication.core.LocaleHelper
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.data.local.AppDatabase
import com.example.mobileapplication.data.remote.RetrofitClient
import com.example.mobileapplication.data.repository.DbImageRepository
import com.example.mobileapplication.data.repository.RoomBookRepository
import com.example.mobileapplication.data.repository.RemoteBookRepository
import com.example.mobileapplication.domain.model.BookImage
import com.example.mobileapplication.ui.screens.AddUserScreen
import com.example.mobileapplication.ui.screens.DetailsScreen
import com.example.mobileapplication.ui.screens.MainScreen
import com.example.mobileapplication.ui.screens.Screen
import com.example.mobileapplication.ui.screens.SettingsScreen
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.example.mobileapplication.ui.viewmodels.ImageViewModel

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

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "books"
        )
            .fallbackToDestructiveMigration()
            .build()

        val fileHelper = FileHelper(applicationContext)

        val apiService = RetrofitClient.apiService
        val bookLocalRepo = RoomBookRepository(db.bookDao())
        val bookRemoteRepo = RemoteBookRepository(apiService, networkHelper)

        val imageLocalRepo = DbImageRepository(db.imageDao())

        val viewModel = BookViewModel(
            networkHelper,
            bookRemoteRepo,
            bookLocalRepo,
            sharedPreferences)

        val imageViewModel = ImageViewModel(
            imageLocalRepo,
            fileHelper
        )

        setContent {
            // Подписка на состояния из ViewModel
            val isRemoteMode by viewModel.isRemoteMode.collectAsState()

            // Работа с темой
            val savedThemeMode = remember {
                mutableIntStateOf(sharedPreferences.getInt("theme_mode", 0))
            }

            val images by imageViewModel.images.collectAsState()

            // Лаунчер для выбора картинки из галереи
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                    uri?.let {imageViewModel.addImage(it)
                }
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
                        MainScreen(navController, viewModel,imageViewModel)
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