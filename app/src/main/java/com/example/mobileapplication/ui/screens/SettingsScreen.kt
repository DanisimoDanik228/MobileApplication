package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.R
import com.example.mobileapplication.auth.NicknameAuthMapper
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: BookViewModel,
    onLanguageChange: (String) -> Unit,
    currentTheme: Int,
    onThemeChange: (Int) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val isNotifEnabled by viewModel.notificationsEnabled.collectAsState()
    val authUser = FirebaseAuth.getInstance().currentUser
    val displayName = authUser?.let { NicknameAuthMapper.displayNickname(it) }.orEmpty()

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
            Text(
                text = stringResource(id = R.string.auth_account),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.auth_signed_in_as, displayName),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.auth_logout))
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.notification),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.notification))
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

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.back))
            }
        }
    }

    if (showTimeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Установить время") },
            text = { Text("Хотите установить ежедневное напоминание на 20:00?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.scheduleNotification(context, 20, 0)
                    showTimeDialog = false
                }) {
                    Text("Да, включить")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showTimeDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
