package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.mobileapplication.data.local.AppDatabase
import com.example.mobileapplication.R
import com.example.mobileapplication.domain.repository.BookRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    itemId: Int,
    navController: NavController,
    db: BookRepository
) {
    val book = remember(itemId) { db.getBookById(itemId) }

    if (book == null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(id = R.string.details_title)) }) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(id = R.string.error_not_found)) // РЕСУРС
                Button(onClick = { navController.popBackStack() }) {
                    Text(stringResource(id = R.string.back)) // РЕСУРС
                }
            }
        }
        return
    }

    var bookName by remember { mutableStateOf(book.bookName) }
    var authorName by remember { mutableStateOf(book.authorName) }
    var description by remember { mutableStateOf(book.description) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.details_viewing_object, book.id),
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
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val updatedBook = book.copy(bookName = bookName, authorName = authorName,description = description)
                    db.updateBook(updatedBook)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.save_changes))
            }

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

            Button(
                onClick = {
                    db.deleteBook(book.id)
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