package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Главный экран") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.Details.createRoute("42")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Открыть детали (ID: 42)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Перейти в настройки")
            }
        }
    }
}