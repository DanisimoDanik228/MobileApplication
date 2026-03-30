package com.example.mobileapplication.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.core.FileHelper
import com.example.mobileapplication.data.repository.DbImageRepository
import com.example.mobileapplication.domain.model.BookImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageViewModel(
    private val repository: DbImageRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _images = MutableStateFlow<List<BookImage>>(emptyList())
    val images: StateFlow<List<BookImage>> = _images

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch(Dispatchers.IO) {
            _images.value = repository.getAllImages()
        }
    }

    // Изменили метод: теперь принимаем Uri из галереи
    fun addImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Сохраняем физический файл в память
            val internalPath = fileHelper.saveImageToInternalStorage(uri)

            // 2. Если файл сохранился, пишем путь в БД Room
            if (internalPath != null) {
                val newImage = BookImage(0, internalPath)
                repository.insertImage(newImage)
                loadImages()
            }
        }
    }

    fun deleteImage(image: BookImage) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Удаляем файл из памяти
            fileHelper.deleteFileFromInternalStorage(image.path)

            // 2. Удаляем запись из базы
            repository.deleteImage(image)
            loadImages()
        }
    }
}