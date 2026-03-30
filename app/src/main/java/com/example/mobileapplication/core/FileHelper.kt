package com.example.mobileapplication.core

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileHelper(private val context: Context) {

    // Копирует файл из URI (галереи) во внутреннюю память приложения
    fun saveImageToInternalStorage(uri: Uri): String? {
        val fileName = "img_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath // Возвращаем путь к новому файлу
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Удаляет файл из памяти
    fun deleteFileFromInternalStorage(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}