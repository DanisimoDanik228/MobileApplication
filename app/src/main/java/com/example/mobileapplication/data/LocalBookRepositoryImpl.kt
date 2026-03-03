package com.example.mobileapplication.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.mobileapplication.domain.Book
import com.example.mobileapplication.domain.BookRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID
import kotlin.collections.addAll
import kotlin.text.clear
import kotlin.text.set

class LocalBookRepositoryImpl(context: Context) : BookRepository {
    private val gson = Gson()
    private val file = File(context.filesDir, "users_data.json")

    val userDatabase = mutableStateListOf<Book>().apply {
        addAll(loadFromFile())
    }

    override fun getAllBooks(): List<Book> = userDatabase

    override fun addBook(book: Book) {
        book.id = UUID.randomUUID().toString()
        userDatabase.add(book)
        saveToFile()
    }

    override fun updateBook(book: Book) {
        val index = userDatabase.indexOfFirst { it.id == book.id }

        if (index != -1) {
            userDatabase[index] = book
            saveToFile()
        }
    }

    override fun deleteBook(id: String ) {
        val removed = userDatabase.removeIf { it.id == id }
        if (removed) {
            saveToFile()
        }
    }

    override fun getBookById(id: String ): Book? {
        return userDatabase.find { it.id == id }
    }

    override fun clearAll() {
        userDatabase.clear()
        saveToFile()
    }

    private fun saveToFile() {
        val jsonString = gson.toJson(userDatabase)
        file.writeText(jsonString)
    }

    private fun loadFromFile(): List<Book> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            val itemType = object : TypeToken<List<Book>>() {}.type
            gson.fromJson(jsonString, itemType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}