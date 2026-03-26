package com.example.mobileapplication.domain.repository

import androidx.room.Query
import androidx.room.Update
import com.example.mobileapplication.domain.model.Book

interface BookRepository {
    fun insertBook(book: Book) : Long

    fun getAllBooks(): List<Book>

    fun deleteBook(id: Int) : Int

    fun deleteAll(): Int

    fun getBookById(id: Int): Book?

    fun updateBook(book: Book): Int
}