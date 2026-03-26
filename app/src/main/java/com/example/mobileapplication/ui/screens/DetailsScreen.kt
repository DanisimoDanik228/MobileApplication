package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.viewmodels.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    itemId: Int,
    navController: NavController,
    viewModel: BookViewModel
) {
    // 1. Подписываемся на состояние книги из ViewModel
    val book by viewModel.selectedBook.collectAsState()

    // 2. Загружаем данные при входе на экран (исправляет ошибку remember { Unit })
    LaunchedEffect(itemId) {
        viewModel.getBookById(itemId)
    }

    // 3. Локальные состояния для редактирования полей
    var bookName by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Обновляем локальные поля, когда книга загрузится из ViewModel
    LaunchedEffect(book) {
        book?.let {
            bookName = it.bookName
            authorName = it.authorName
            description = it.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Если книга еще грузится или не найдена
        if (book == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Или текст "Книга не найдена"
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.details_viewing_object, book!!.id),
                    style = MaterialTheme.typography.labelLarge
                )

                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    label = { Text(stringResource(id = R.string.label_book_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text(stringResource(id = R.string.label_author_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(id = R.string.label_description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // КНОПКА СОХРАНИТЬ
                Button(
                    onClick = {
                        val updatedBook = book!!.copy(
                            bookName = bookName,
                            authorName = authorName,
                            description = description
                        )
                        viewModel.updateBook(updatedBook) // Через ViewModel
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.save_changes))
                }

                // КНОПКА ОЧИСТИТЬ
                OutlinedButton(
                    onClick = {
                        bookName = ""
                        authorName = ""
                        description = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.clear_fields))
                }

                // КНОПКА УДАЛИТЬ
                Button(
                    onClick = {
                        viewModel.deleteBook(book!!.id) // Через ViewModel
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.delete_record))
                }
            }
        }
    }
}