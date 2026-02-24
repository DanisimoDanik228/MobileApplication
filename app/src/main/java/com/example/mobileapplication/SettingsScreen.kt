package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, onLanguageChange: (String) -> Unit) {
    val context = LocalContext.current // Получаем текущий контекст

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) }) // Используем stringResource
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(stringResource(id = R.string.settings_language_label), style = MaterialTheme.typography.bodyLarge) // Используем stringResource

            Spacer(modifier = Modifier.height(8.dp))

            Row { // Кнопки языка в ряд
                Button(onClick = { onLanguageChange("ru") },
                    modifier = Modifier.padding(end = 8.dp)) {
                    Text(stringResource(id = R.string.russian)) // Используем stringResource
                }

                Button(onClick = { onLanguageChange("en") }) {
                    Text(stringResource(id = R.string.english)) // Используем stringResource
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text(stringResource(id = R.string.back)) // Используем stringResource
            }
        }
    }
}