package com.example.mobileapplication.domain

interface BookRepository {
    fun getBookById(id: String ) : Book?
    fun getAllBooks() : List<Book>
    fun addBook(book: Book)
    fun updateBook(book: Book)
    fun deleteBook(id: String )

    fun clearAll()
}