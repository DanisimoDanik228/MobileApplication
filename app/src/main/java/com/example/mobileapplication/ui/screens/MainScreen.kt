package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // Нужно добавить импорт
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.example.mobileapplication.R
import com.example.mobileapplication.core.Constants
import com.example.mobileapplication.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: BookViewModel) {
    val books by viewModel.books.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val weatherTemp by viewModel.weather.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchWeather()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.main_title),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 1. Индикатор сети
                        NetworkStatusBadge(isOnline = isOnline, ping = ping)

                        Spacer(modifier = Modifier.width(8.dp))

                        // 2. ТВОЯ ПОГОДА (Вставляем сюда)
                        WeatherBadge(temp = weatherTemp)
                    }
                },
                // КНОПКА ОБНОВЛЕНИЯ ЗДЕСЬ
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.getAllBooks() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddUser.route) }) {
                Text("+")
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
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.go_to_settings))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (books.isEmpty()) {
                // Можно добавить заглушку, если список пуст
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Список пуст")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(books.size) { index ->
                        val book = books[index]
                        UserItem(
                            user = book,
                            onClick = {
                                navController.navigate(Screen.Details.createRoute(book.id.toString()))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherBadge(temp: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(text = "☁", fontSize = 12.sp) // Можно заменить на иконку
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Constants.CITY_WEATHER + ": $temp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserItem(user: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.user_id, user.id),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = stringResource(R.string.book_title, user.bookName),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.book_author, user.authorName),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.book_description, user.description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NetworkStatusBadge(isOnline: Boolean, ping: String) {
    Surface(
        color = if (isOnline) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color.Red,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "Online | $ping" else "Offline",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}