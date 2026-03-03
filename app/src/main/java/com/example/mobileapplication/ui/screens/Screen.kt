package com.example.mobileapplication.ui.screens

sealed class Screen(val route: String) {
     object Main : Screen("main")

    object Settings : Screen("settings")

    object AddUser : Screen("add_user")

    object Details : Screen("details/{itemId}") {
       fun createRoute(itemId: String) = "details/$itemId"
    }
}