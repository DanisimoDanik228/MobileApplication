package com.example.mobileapplication.core

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileHelper {
    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "book_img_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file.absolutePath
        } catch (e: Exception) { null }
    }
}