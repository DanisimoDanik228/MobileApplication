package com.example.mobileapplication

sealed class Screen(val route: String) {
    // Главный экран - маршрут просто "main"
    object Main : Screen("main")

    // Экран настроек - маршрут "settings"
    object Settings : Screen("settings")

    // Экран деталей - маршрут "details/{itemId}"
    // Фигурные скобки {itemId} означают, что это переменная часть пути
    object Details : Screen("details/{itemId}") {
        // Эта функция нужна, чтобы удобно передавать ID при переходе
        // Например: Screen.Details.createRoute("123") вернет строку "details/123"
        fun createRoute(itemId: String) = "details/$itemId"
    }
}