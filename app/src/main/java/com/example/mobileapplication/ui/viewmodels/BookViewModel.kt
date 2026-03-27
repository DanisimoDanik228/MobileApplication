package com.example.mobileapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.core.Constants
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.data.remote.WeatherApiService
import com.example.mobileapplication.domain.model.Book
import com.example.mobileapplication.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookViewModel(
    private val networkHelper: NetworkHelper,
    private val remoteRepo: BookRepository,
    private val localRepo: BookRepository
) : ViewModel() {

    // 1. Сначала объявляем ВСЕ переменные состояния
    private val _isRemoteMode = MutableStateFlow(true)
    val isRemoteMode = _isRemoteMode.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _ping = MutableStateFlow("-")
    val ping = _ping.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Теперь это выше init
    val isLoading = _isLoading.asStateFlow()

    private val currentRepo: BookRepository
        get() = if (_isRemoteMode.value) remoteRepo else localRepo

    // 2. И только в самом конце вызываем init
    init {
        startNetworkMonitoring()
        getAllBooks()
    }

    private val _weather = MutableStateFlow("..°C")
    val weather: StateFlow<String> = _weather.asStateFlow()

    // Создай отдельный Retrofit для погоды (так как URL другой)
    private val weatherApi = Retrofit.Builder()
        .baseUrl(Constants.WEATHER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather()
                val temp = response.main.temp.toInt()
                _weather.value = "$temp°C"
            } catch (e: Exception) {
                _weather.value = "Err"
            }
        }
    }

    fun setRepositoryMode(isRemote: Boolean) {
        _isRemoteMode.value = isRemote
        getAllBooks() // Сразу обновляем список под новый источник
    }

    // 3. Далее идут функции
    private fun startNetworkMonitoring() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val online = networkHelper.isNetworkAvailable()
                _isOnline.value = online
                _ping.value = if (online) networkHelper.getPing() else "∞"
                delay(Constants.NETWORK_CHECK_FREQUENCY)
            }
        }
    }

    fun getAllBooks() {
        viewModelScope.launch {
            _isLoading.value = true // Теперь тут не будет ошибки
            try {
                _books.value = currentRepo.getAllBooks()
            } catch (e: Exception) {
                // Хорошо бы добавить обработку ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. ДОБАВИТЬ КНИГУ
    fun insertBook(name: String, author: String, desc: String) {
        viewModelScope.launch {
            val book = Book(id = 0, bookName = name, authorName = author, description = desc)
            val newId = currentRepo.insertBook(book) // Возвращает Long (ID в Room)
            if (newId > 0) {
                getAllBooks() // Обновляем список, если добавлено успешно
            }
        }
    }

    // 3. УДАЛИТЬ КНИГУ ПО ID
    fun deleteBook(id: Int) {
        viewModelScope.launch {
            val deletedRows = currentRepo.deleteBook(id) // Возвращает Int (количество удаленных)
            if (deletedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }

    // 5. ПОЛУЧИТЬ КНИГУ ПО ID (например, для открытия деталей)
    fun getBookById(id: Int) {
        viewModelScope.launch {
            val book = currentRepo.getBookById(id)
            _selectedBook.value = book
        }
    }

    // 6. ОБНОВИТЬ КНИГУ
    fun updateBook(book: Book) {
        viewModelScope.launch {
            val updatedRows = currentRepo.updateBook(book) // Возвращает Int
            if (updatedRows > 0) {
                getAllBooks() // Обновляем список
            }
        }
    }
}