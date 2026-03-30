package com.example.mobileapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.data.repository.DbImageRepository
import com.example.mobileapplication.domain.model.BookImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageViewModel(private val repository: DbImageRepository) : ViewModel() {

    // Состояние списка изображений для UI
    private val _images = MutableStateFlow<List<BookImage>>(emptyList())
    val images: StateFlow<List<BookImage>> = _images

    init {
        loadImages()
    }

    // READ
    fun loadImages() {
        viewModelScope.launch {
            _images.value = repository.getAllImages()
        }
    }

    // CREATE
    fun addImage(image: BookImage) {
        viewModelScope.launch {
            repository.insertImage(image)
            loadImages() // Обновляем список после добавления
        }
    }

    // UPDATE
    fun updateImage(image: BookImage) {
        viewModelScope.launch {
            repository.updateImage(image)
            loadImages() // Обновляем список
        }
    }

    // DELETE
    fun deleteImage(image: BookImage) {
        viewModelScope.launch {
            repository.deleteImage(image)
            loadImages() // Обновляем список
        }
    }
}