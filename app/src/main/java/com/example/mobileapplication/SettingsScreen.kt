package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onLanguageChange: (String) -> Unit,
    currentTheme: Int,
    onThemeChange: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_language_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { onLanguageChange("ru") }, modifier = Modifier.padding(end = 8.dp)) {
                    Text(stringResource(id = R.string.russian))
                }
                Button(onClick = { onLanguageChange("en") }) {
                    Text(stringResource(id = R.string.english))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.theme_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentTheme == 1, onClick = { onThemeChange(1) })
                    Text(stringResource(id = R.string.theme_light), modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentTheme == 2, onClick = { onThemeChange(2) })
                    Text(stringResource(id = R.string.theme_dark), modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = currentTheme == 0, onClick = { onThemeChange(0) })
                    Text(stringResource(id = R.string.theme_system), modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.back))
            }
        }
    }
}