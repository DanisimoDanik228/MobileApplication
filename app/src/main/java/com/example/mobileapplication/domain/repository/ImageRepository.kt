package com.example.mobileapplication.domain.repository

import com.example.mobileapplication.domain.model.BookImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ImageRepository {
    suspend fun getAllImages(): List<BookImage>

    suspend fun insertImage(image: BookImage)

    suspend fun updateImage(image: BookImage)

    suspend fun deleteImage(image: BookImage)
}