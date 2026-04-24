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
import coil.compose.AsyncImage
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
    imageViewModel: ImageViewModel
) {
    val books by viewModel.filteredBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val weatherTemp by viewModel.weather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    // Все картинки из базы данных
    val allImages by imageViewModel.images.collectAsState()

    // Состояние: для какой книги мы сейчас выбираем картинку
    var selectedBookIdForImage by remember { mutableStateOf<Int?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Если ID выбран, добавляем картинку именно для этой книги
            selectedBookIdForImage?.let { bookId ->
                imageViewModel.addImage(it, bookId)
            }
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
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(id = R.string.go_to_settings))
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            if (books.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isEmpty()) "Список пуст" else "Ничего не найдено")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(books) { book ->
                        // Фильтруем картинки: оставляем только те, что привязаны к этой книге
                        // Убедитесь, что в BookImage есть поле bookId
                        val bookSpecificImages = allImages.filter { it.bookId == book.id }

                        UserItem(
                            user = book,
                            images = bookSpecificImages,
                            onAddImageClick = {
                                selectedBookIdForImage = book.id
                                imagePickerLauncher.launch("image/*")
                            },
                            onDeleteImage = { imageViewModel.deleteImage(it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserItem(
    user: Book,
    images: List<BookImage>,
    onAddImageClick: () -> Unit,
    onDeleteImage: (BookImage) -> Unit,
    onClick: () -> Unit
) {
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

            Spacer(modifier = Modifier.height(12.dp))

            // Ряд с кнопкой добавления и картинками конкретной книги
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка добавления персонального фото
                item {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onAddImageClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                // Список уже добавленных фото для этой книги
                items(images) { img ->
                    Box(modifier = Modifier.size(60.dp)) {
                        AsyncImage(
                            model = img.path,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Кнопка удаления на фото
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .clickable { onDeleteImage(img) },
                            shape = CircleShape,
                            color = Color.Red
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp).padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ---

@OptIn(ExperimentalMaterial3Api::class)
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
                    .background(Color.White, CircleShape)
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