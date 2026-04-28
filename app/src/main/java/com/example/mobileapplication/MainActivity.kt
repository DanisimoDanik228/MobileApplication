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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobileapplication.core.LocaleHelper
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.data.repository.BookFirestoreRepository
import com.example.mobileapplication.ui.screens.AddUserScreen
import com.example.mobileapplication.ui.screens.AuthScreen
import com.example.mobileapplication.ui.screens.DetailsScreen
import com.example.mobileapplication.ui.screens.MainScreen
import com.example.mobileapplication.ui.screens.Screen
import com.example.mobileapplication.ui.screens.SettingsScreen
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        val networkHelper = NetworkHelper(applicationContext)
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        val bookRepo = BookFirestoreRepository(applicationContext)

        val viewModel = BookViewModel(
            networkHelper,
            bookRepo,
            sharedPreferences
        )

        setContent {
            val auth = remember { FirebaseAuth.getInstance() }
            var firebaseUser by remember { mutableStateOf(auth.currentUser) }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { a ->
                    firebaseUser = a.currentUser
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            val savedThemeMode = remember {
                mutableIntStateOf(sharedPreferences.getInt("theme_mode", 0))
            }

            val isDarkTheme = when (savedThemeMode.intValue) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            MobileApplicationTheme(darkTheme = isDarkTheme) {
                if (firebaseUser == null) {
                    AuthScreen(onAuthenticated = { firebaseUser = auth.currentUser })
                } else {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(navController, viewModel)
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                viewModel = viewModel,
                                onLanguageChange = { lang -> changeLanguage(lang) },
                                currentTheme = savedThemeMode.intValue,
                                onThemeChange = { newTheme ->
                                    sharedPreferences.edit().putInt("theme_mode", newTheme).apply()
                                    savedThemeMode.intValue = newTheme
                                },
                                onSignOut = {
                                    auth.signOut()
                                    firebaseUser = null
                                }
                            )
                        }

                        composable(
                            route = Screen.Details.route,
                            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val idString = backStackEntry.arguments?.getString("itemId")
                            val id = idString?.toLongOrNull() ?: 0L
                            DetailsScreen(id, navController, viewModel)
                        }

                        composable(Screen.AddUser.route) {
                            AddUserScreen(navController, viewModel)
                        }
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
