package com.example.mobileapplication.domain.repository

import androidx.room.Query
import androidx.room.Update
import com.example.mobileapplication.domain.model.Book

interface BookRepository {
    suspend fun insertBook(book: Book) : Long

    suspend fun getAllBooks(): List<Book>

    suspend fun deleteBook(id: Int) : Int

    suspend fun getBookById(id: Int): Book?

    suspend fun updateBook(book: Book): Int
}