package com.example.mobileapplication.Repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.reflect.TypeToken
import java.io.File
import com.google.gson.Gson

class LocalUserRepositoryImpl(context: Context) : UserRepository {
    private val gson = Gson()
    // Путь к файлу во внутреннем хранилище приложения
    private val file = File(context.filesDir, "users_data.json")

    // Список теперь загружается из файла при создании репозитория
    val userDatabase = mutableStateListOf<User>().apply {
        addAll(loadFromFile())
    }

    override fun getAllUsers(): List<User> = userDatabase
    override fun addUser(user: User) {
        userDatabase.add(user)
        saveToFile() // Сразу сохраняем изменения в файл
    }

    override fun getUserById(id: Int): User? {
        return userDatabase.find { it.id == id }
    }

    override fun saveUser(user: User) {
        userDatabase.add(user)
        saveToFile() // Сохраняем изменения в файл сразу
    }

    // Сохранение списка в JSON
    private fun saveToFile() {
        val jsonString = gson.toJson(userDatabase)
        file.writeText(jsonString)
    }

    // Загрузка списка из JSON
    private fun loadFromFile(): List<User> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            val itemType = object : TypeToken<List<User>>() {}.type
            gson.fromJson(jsonString, itemType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}