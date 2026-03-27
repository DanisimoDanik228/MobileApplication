package com.example.mobileapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.core.Constants
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookViewModel(
    private val networkHelper: NetworkHelper,
    private val remoteRepo: BookRepository,
    private val localRepo: BookRepository
) : ViewModel() {
    private val _isRemoteMode = MutableStateFlow(true)
    val isRemoteMode = _isRemoteMode.asStateFlow()

    // Вспомогательное свойство, которое выбирает текущий репозиторий
    private val currentRepo: BookRepository
        get() = if (_isRemoteMode.value) remoteRepo else localRepo

    fun setRepositoryMode(isRemote: Boolean) {
        _isRemoteMode.value = isRemote
        getAllBooks() // Сразу обновляем список под новый источник
    }

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _ping = MutableStateFlow("-")
    val ping = _ping.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    // Состояние конкретной книги (например, для экрана деталей)
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    init {
        startNetworkMonitoring()
        getAllBooks()
    }

    private fun startNetworkMonitoring() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val online = networkHelper.isNetworkAvailable()
                _isOnline.value = online
                if (online) {
                    _ping.value = networkHelper.getPing()
                } else {
                    _ping.value = "∞"
                }

                delay(Constants.NETWORK_CHECK_FREQUENCY) // Проверяем каждые 5 секунд
            }
        }
    }

    // 1. ПОЛУЧИТЬ ВСЕ КНИГИ
    fun getAllBooks() {
        viewModelScope.launch {
            val list = currentRepo.getAllBooks()
            _books.value = list
        }
    }

    // 2. ДОБАВИТЬ КНИГУ
    fun insertBook(name: String, author: String, desc: String) {
        viewModelScope.launch {
            val book = Book(id = 0, bookName = name, authorName = author, description = desc)
            val newId = currentRepo.insertBook(book) // Возвращает Long (ID в Room)
            if (newId > 0) {
                getAllBooks() // Обновляем список, если добавлено успешно
            }
        }
    }

    // 3. УДАЛИТЬ КНИГУ ПО ID
    fun deleteBook(id: Int) {
        viewModelScope.launch {
            val deletedRows = currentRepo.deleteBook(id) // Возвращает Int (количество удаленных)
            if (deletedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }

    // 5. ПОЛУЧИТЬ КНИГУ ПО ID (например, для открытия деталей)
    fun getBookById(id: Int) {
        viewModelScope.launch {
            val book = currentRepo.getBookById(id)
            _selectedBook.value = book
        }
    }

    // 6. ОБНОВИТЬ КНИГУ
    fun updateBook(book: Book) {
        viewModelScope.launch {
            val updatedRows = currentRepo.updateBook(book) // Возвращает Int
            if (updatedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }
}