package com.example.mobileapplication.core

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class InternalStorageManager(private val context: Context) {

    // Сохраняет Bitmap и возвращает абсолютный путь
    fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        return try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Удаляет файл при удалении записи из БД
    fun deleteImageFile(path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
    }
}