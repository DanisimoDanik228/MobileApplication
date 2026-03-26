package com.example.mobileapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // Состояние списка книг для отображения на экране
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    // Состояние конкретной книги (например, для экрана деталей)
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    init {
        // Автоматическая загрузка при старте
        getAllBooks()
    }

    // 1. ПОЛУЧИТЬ ВСЕ КНИГИ
    fun getAllBooks() {
        viewModelScope.launch {
            val list = repository.getAllBooks()
            _books.value = list
        }
    }

    // 2. ДОБАВИТЬ КНИГУ
    fun insertBook(name: String, author: String, desc: String) {
        viewModelScope.launch {
            val book = Book(id = 0, bookName = name, authorName = author, description = desc)
            val newId = repository.insertBook(book) // Возвращает Long (ID в Room)
            if (newId > 0) {
                getAllBooks() // Обновляем список, если добавлено успешно
            }
        }
    }

    // 3. УДАЛИТЬ КНИГУ ПО ID
    fun deleteBook(id: Int) {
        viewModelScope.launch {
            val deletedRows = repository.deleteBook(id) // Возвращает Int (количество удаленных)
            if (deletedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }

    // 5. ПОЛУЧИТЬ КНИГУ ПО ID (например, для открытия деталей)
    fun getBookById(id: Int) {
        viewModelScope.launch {
            val book = repository.getBookById(id)
            _selectedBook.value = book
        }
    }

    // 6. ОБНОВИТЬ КНИГУ
    fun updateBook(book: Book) {
        viewModelScope.launch {
            val updatedRows = repository.updateBook(book) // Возвращает Int
            if (updatedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }
}