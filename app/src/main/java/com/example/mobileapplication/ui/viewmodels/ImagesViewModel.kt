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

    // 1. Добавили параметр bookId с дефолтным значением 0
    fun addImage(uri: Uri, bookId: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = fileHelper.saveImageToInternalStorage(uri)

            if (internalPath != null) {
                // 2. ИСПРАВЛЕНО: используем именованные параметры, чтобы не запутаться
                val newImage = BookImage(
                    bookId = bookId, // Теперь это Int
                    path = internalPath // Теперь это String
                )
                repository.insertImage(newImage)
                loadImages()
            }
        }
    }

    fun deleteImage(image: BookImage) {
        viewModelScope.launch(Dispatchers.IO) {
            fileHelper.deleteFileFromInternalStorage(image.path)
            repository.deleteImage(image)
            loadImages()
        }
    }
}