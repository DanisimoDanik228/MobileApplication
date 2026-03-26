package com.example.mobileapplication.data.repository

import android.util.Log
import com.example.mobileapplication.data.remote.BookApiService
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository

class RemoteBookRepository(
    private val apiService: BookApiService
) : BookRepository {

    // 1. Добавить книгу на сервер
    override suspend fun insertBook(book: Book): Long {
        return try {
            val createdBook = apiService.postBook(book)
            // Возвращаем ID созданной сервером книги
            createdBook.id.toLong()
        } catch (e: Exception) {
            Log.e("REPO", "Ошибка при вставке на сервер: ${e.message}")
            0L
        }
    }

    // 2. Получить все книги напрямую с сервера
    override suspend fun getAllBooks(): List<Book> {
        return try {
            apiService.getBooks()
        } catch (e: Exception) {
            Log.e("REPO", "Ошибка при получении списка: ${e.message}")
            emptyList() // Если сервер недоступен, возвращаем пустой список
        }
    }

    // 3. Удалить книгу по ID на сервере
    override suspend fun deleteBook(id: Int): Int {
        return try {
            val response = apiService.deleteBook(id)
            if (response.isSuccessful) 1 else 0
        } catch (e: Exception) {
            Log.e("REPO", "Ошибка при удалении книги: ${e.message}")
            0
        }
    }

    // 5. Получить конкретную книгу по ID с сервера
    override suspend fun getBookById(id: Int): Book? {
        return try {
            val response = apiService.getBook(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("REPO", "Ошибка при поиске книги: ${e.message}")
            null
        }
    }

    // 6. Обновить книгу на сервере
    override suspend fun updateBook(book: Book): Int {
        return try {
            val response = apiService.putBook(book.id, book)
            // Если код ответа 200-299, считаем что обновили 1 запись
            if (response.isSuccessful) 1 else 0
        } catch (e: Exception) {
            Log.e("REPO", "Ошибка при обновлении книги: ${e.message}")
            0
        }
    }
}