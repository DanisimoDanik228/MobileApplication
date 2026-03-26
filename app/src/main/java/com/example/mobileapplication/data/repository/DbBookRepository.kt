package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.local.BookDao
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository

class DbBookRepository(private val bookDao: BookDao) : BookRepository {
    override fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    override fun getAllBooks(): List<Book> {
        return bookDao.getAllBooks()
    }

    override fun deleteBook(id: Int): Int {
        return bookDao.deleteBook(id)
    }

    override fun deleteAll(): Int {
        return bookDao.deleteAll()
    }

    override fun getBookById(id: Int): Book? {
        return bookDao.getBookById(id)
    }

    override fun updateBook(book: Book): Int {
        return bookDao.updateBook(book)
    }
}