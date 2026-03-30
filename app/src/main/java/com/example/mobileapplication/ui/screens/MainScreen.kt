package com.example.mobileapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage // Не забудьте добавить зависимость Coil в build.gradle
import com.example.mobileapplication.R
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.model.BookImage
import com.example.mobileapplication.ui.viewmodels.BookSortOrder
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.example.mobileapplication.ui.viewmodels.ImageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: BookViewModel,
    imageViewModel: ImageViewModel // 1. Добавили ViewModel для картинок
) {
    val books by viewModel.filteredBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val weatherTemp by viewModel.weather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    // 2. Состояние списка картинок
    val images by imageViewModel.images.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Просто передаем Uri. ViewModel сама сохранит его в файл и в БД
            imageViewModel.addImage(it)
        }
    }
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
                        WeatherBadge(city = currentCity, temp = weatherTemp, onClick = { viewModel.toggleCity() })
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
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- СЕКЦИЯ КАРТИНОК (В САМОМ ВЕРХУ) ---
            Text("Галерея", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка добавления
                item {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                // Список картинок из БД
                items(images) { img ->
                    Box(modifier = Modifier.size(70.dp)) {
                        AsyncImage(
                            model = img.path,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Кнопка удаления на картинке
                        IconButton(
                            onClick = { imageViewModel.deleteImage(img) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка настроек
            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(id = R.string.go_to_settings))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ПОИСК
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // СОРТИРОВКА
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сорт:", style = MaterialTheme.typography.labelMedium)
                SortChip("Имя", sortOrder == BookSortOrder.TITLE) { viewModel.sortOrder.value = BookSortOrder.TITLE }
                SortChip("Автор", sortOrder == BookSortOrder.AUTHOR) { viewModel.sortOrder.value = BookSortOrder.AUTHOR }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // СПИСОК КНИГ
            if (books.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isEmpty()) "Список пуст" else "Ничего не найдено")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
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

// ... Ваши остальные Composable (SortChip, WeatherBadge, UserItem, NetworkStatusBadge) остаются без изменений

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