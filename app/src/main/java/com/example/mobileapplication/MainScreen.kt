package com.example.mobileapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.Repository.LocalUserRepositoryImpl
import com.example.mobileapplication.Repository.User
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController,repository : LocalUserRepositoryImpl) {
    val users = repository.getAllUsers()

    Scaffold(
        topBar = {
            TopAppBar(title = {  Text(stringResource(id = R.string.main_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddUser.route) }) {
                Text("+") // Или иконка Icons.Default.Add
            }
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
                Text(stringResource(id = R.string.details))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.go_to_settings))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Отступы между элементами
            ) {
                items(users.size) { i ->
                    UserItem(users[i]) // Вызываем отдельный UI для строки
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${user.id}")
            Text(text = "Имя: ${user.name}")
        }
    }
}