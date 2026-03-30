package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.local.ImageDao
import com.example.mobileapplication.domain.model.BookImage
import com.example.mobileapplication.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbImageRepository(private val imageDao: ImageDao) : ImageRepository {

    // Выполняем в потоке IO, так как в DAO это обычная функция List (не Flow)
    override suspend fun getAllImages(): List<BookImage> = withContext(Dispatchers.IO) {
        imageDao.getAllImages()
    }

    override suspend fun insertImage(image: BookImage) = withContext(Dispatchers.IO) {
        imageDao.insertImage(image)
    }

    override suspend fun updateImage(image: BookImage) = withContext(Dispatchers.IO) {
        imageDao.updateImage(image)
    }

    override suspend fun deleteImage(image: BookImage) = withContext(Dispatchers.IO) {
        imageDao.deleteImage(image)
    }
}