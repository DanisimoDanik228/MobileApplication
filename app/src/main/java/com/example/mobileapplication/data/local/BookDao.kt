package com.example.mobileapplication.data.local
import com.example.mobileapplication.domain.model.Book
import androidx.room.*
import com.example.mobileapplication.domain.repository.BookRepository

@Dao
interface BookDao {
    @Insert
    fun insertBook(book: Book) : Long

    @Query("SELECT * FROM books")
    fun getAllBooks(): List<Book>

    @Query("DELETE FROM books WHERE id = :id")
    fun deleteBook(id: Int) : Int

    @Query("DELETE FROM books")
    fun deleteAll(): Int

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Int): Book?

    @Update
    fun updateBook(book: Book): Int
}