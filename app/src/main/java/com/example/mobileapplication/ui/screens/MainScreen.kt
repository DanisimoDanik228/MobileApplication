package com.example.mobileapplication.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.mobileapplication.core.BookCoverImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.mobileapplication.ui.viewmodels.BookSortOrder
import com.example.mobileapplication.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: BookViewModel
) {
    val books by viewModel.filteredBooks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val weatherTemp by viewModel.weather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    val context = LocalContext.current

    var selectedBookIdForImage by remember { mutableStateOf<Long?>(null) }
    var showCoverSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        val bookId = selectedBookIdForImage
        if (success && uri != null && bookId != null) {
            viewModel.setBookCoverFromUri(bookId, uri.toString())
        }
        pendingCameraUri = null
        selectedBookIdForImage = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = BookCoverImageCapture.createImageUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
            selectedBookIdForImage = null
        }
    }

    fun openCameraCapture() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                val uri = BookCoverImageCapture.createImageUri(context)
                pendingCameraUri = uri
                takePictureLauncher.launch(uri)
            }
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val bookId = selectedBookIdForImage
        uri?.let { u ->
            bookId?.let { id ->
                viewModel.setBookCoverFromUri(id, u.toString())
            }
        }
        selectedBookIdForImage = null
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
                        IconButton(onClick = { viewModel.refreshBooks() }) {
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
                    items(books, key = { it.id }) { book ->
                        BookItem(
                            book = book,
                            onAddImageClick = {
                                selectedBookIdForImage = book.id
                                showCoverSourceDialog = true
                            },
                            onClick = {
                                navController.navigate(Screen.Details.createRoute(book.id.toString()))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCoverSourceDialog) {
        AlertDialog(
            onDismissRequest = {
                showCoverSourceDialog = false
                selectedBookIdForImage = null
            },
            title = { Text(stringResource(R.string.book_cover_source_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            showCoverSourceDialog = true
                            //imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.book_cover_from_gallery))
                    }
                    TextButton(
                        onClick = {
                            showCoverSourceDialog = false
                            openCameraCapture()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.book_cover_take_photo))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCoverSourceDialog = false
                        selectedBookIdForImage = null
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookItem(
    book: Book,
    onAddImageClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onAddImageClick),
                contentAlignment = Alignment.Center
            ) {
                if (!book.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = book.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ID: ${book.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = book.bookName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (book.authorName.isNotEmpty()) {
                    Text(
                        text = book.authorName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

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
