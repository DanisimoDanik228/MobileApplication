package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(itemId: String?, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Детали") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Вы смотрите объект с ID: $itemId", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
    }
}