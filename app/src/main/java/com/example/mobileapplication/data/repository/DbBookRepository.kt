package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.local.BookDao
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository

class DbBookRepository(private val bookDao: BookDao) : BookRepository {
    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    override suspend fun getAllBooks(): List<Book> {
        return bookDao.getAllBooks()
    }

    override suspend fun deleteBook(id: Int): Int {
        return bookDao.deleteBook(id)
    }

    override suspend fun getBookById(id: Int): Book? {
        return bookDao.getBookById(id)
    }

    override suspend fun updateBook(book: Book): Int {
        return bookDao.updateBook(book)
    }
}