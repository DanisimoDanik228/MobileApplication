package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileapplication.R
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.ui.viewmodels.BookSortOrder
import com.example.mobileapplication.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: BookViewModel) {
    // Подписки на состояния из ViewModel
    val books by viewModel.filteredBooks.collectAsState() // Используем отфильтрованный список
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val weatherTemp by viewModel.weather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.main_title),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        NetworkStatusBadge(isOnline = isOnline, ping = ping)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Кликабельная погода
                        WeatherBadge(
                            city = currentCity,
                            temp = weatherTemp,
                            onClick = { viewModel.toggleCity() }
                        )
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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
                Text("+", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Кнопка перехода в настройки
            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(id = R.string.go_to_settings))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. ПОЛЕ ПОИСКА
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск (название, автор)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. СОРТИРОВКА (Чипы)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сорт:", style = MaterialTheme.typography.labelMedium)
                SortChip("Имя", sortOrder == BookSortOrder.TITLE) { viewModel.sortOrder.value = BookSortOrder.TITLE }
                SortChip("Автор", sortOrder == BookSortOrder.AUTHOR) { viewModel.sortOrder.value = BookSortOrder.AUTHOR }
                SortChip("ID", sortOrder == BookSortOrder.ID) { viewModel.sortOrder.value = BookSortOrder.ID }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. СПИСОК КНИГ
            if (books.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isEmpty()) "Список пуст" else "Ничего не найдено")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Отступ под FAB
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
fun SortChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp) },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun WeatherBadge(city: String, temp: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "☁", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$city: $temp",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ID: ${user.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = user.bookName,
                style = MaterialTheme.typography.titleMedium
            )
            if (user.authorName.isNotEmpty()) {
                Text(
                    text = user.authorName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun NetworkStatusBadge(isOnline: Boolean, ping: String) {
    Surface(
        color = if (isOnline) Color(0xFF4CAF50) else Color.Red,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) ping else "Offline",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}