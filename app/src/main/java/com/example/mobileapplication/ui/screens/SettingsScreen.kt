package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: BookViewModel,
    onLanguageChange: (String) -> Unit,
    currentTheme: Int,
    onThemeChange: (Int) -> Unit,
    isRemoteMode: Boolean,
    onRepositoryModeChange: (Boolean) -> Unit
) {
    // Контекст нужен для работы с будильником (AlarmManager)
    val context = LocalContext.current
    val isNotifEnabled by viewModel.notificationsEnabled.collectAsState()

    // Стейт для показа диалога выбора времени
    var showTimeDialog by remember { mutableStateOf(false) }

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
            // --- СЕКЦИЯ ЯЗЫКА ---
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

            // --- СЕКЦИЯ ТЕМЫ ---
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- СЕКЦИЯ БАЗЫ ДАННЫХ ---
            Text(
                text = stringResource(id = R.string.database_mode_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isRemoteMode, onClick = { onRepositoryModeChange(true) })
                    Text(text = stringResource(id = R.string.database_mode_remote), modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isRemoteMode, onClick = { onRepositoryModeChange(false) })
                    Text(text = stringResource(id = R.string.database_mode_local), modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- НОВАЯ СЕКЦИЯ: УВЕДОМЛЕНИЯ ---
            Text(
                text = "Notification", // Можно добавить в strings.xml
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notification")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isNotifEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            viewModel.startMinuteNotifications(context)
                        } else {
                            viewModel.stopNotifications(context)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // КНОПКА НАЗАД
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.back))
            }
        }
    }

    // Диалог подтверждения установки времени
    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Установить время") },
            text = { Text("Хотите установить ежедневное напоминание на 20:00?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.scheduleNotification(context, 20, 0)
                    showTimeDialog = false
                }) {
                    Text("Да, включить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}