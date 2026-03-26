package com.example.mobileapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobileapplication.ui.viewmodels.BookViewModel
import com.example.mobileapplication.data.local.AppDatabase
import com.example.mobileapplication.R
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: BookViewModel) {
    val books by viewModel.books.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.main_title)) })
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