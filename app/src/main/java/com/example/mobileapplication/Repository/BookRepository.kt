package com.example.mobileapplication.Repository

interface BookRepository {
    fun getBookById(id: String ) : Book?
    fun getAllBooks() : List<Book>
    fun addBook(book: Book)
    fun updateBook(book: Book)
    fun deleteBook(id: String )

    fun clearAll()
}