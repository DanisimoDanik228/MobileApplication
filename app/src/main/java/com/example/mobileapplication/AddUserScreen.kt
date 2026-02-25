package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.Repository.LocalUserRepositoryImpl
import com.example.mobileapplication.Repository.User
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(navController: NavController, repository: LocalUserRepositoryImpl) {
    var userId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }

    // Состояние прокрутки
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Добавить пользователя") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // 1. Добавляем автоматический отступ от клавиатуры
                .imePadding()
                // 2. Делаем экран прокручиваемым, если контент не влезает
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Введите ID") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Введите имя") },
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Используем Spacer с весом, чтобы кнопка была внизу,
            // но при появлении клавиатуры она "подпрыгивала" вверх
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val idInt = userId.toIntOrNull()
                    if (idInt != null && userName.isNotBlank()) {
                        repository.addUser(User(idInt, userName))
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}