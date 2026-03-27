package com.example.mobileapplication.data.local
import com.example.mobileapplication.domain.model.Book
import androidx.room.*
import com.example.mobileapplication.domain.repository.BookRepository

@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: Book) : Long

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Int) : Int

    @Query("DELETE FROM books")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    @Update
    suspend fun updateBook(book: Book): Int
}