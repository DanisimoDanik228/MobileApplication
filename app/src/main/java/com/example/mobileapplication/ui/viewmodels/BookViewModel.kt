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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookViewModel(
    private val networkHelper: NetworkHelper,
    private val remoteRepo: BookRepository,
    private val localRepo: BookRepository
) : ViewModel() {

    // --- СОСТОЯНИЯ СЕТИ И ПОГОДЫ ---
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _ping = MutableStateFlow("-")
    val ping = _ping.asStateFlow()

    private val _currentCity = MutableStateFlow(Constants.CITY_WEATHER)
    val currentCity = _currentCity.asStateFlow()

    private val _weather = MutableStateFlow("..°C")
    val weather = _weather.asStateFlow()

    // --- СОСТОЯНИЯ ДАННЫХ ---
    private val _isRemoteMode = MutableStateFlow(true)
    val isRemoteMode = _isRemoteMode.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList()) // Исходный список

    val searchQuery = MutableStateFlow("") // Текст поиска
    val sortOrder = MutableStateFlow(BookSortOrder.TITLE) // Тип сортировки

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private var currentRepo: BookRepository = remoteRepo

    // --- ФИЛЬТРАЦИЯ И СОРТИРОВКА (Combine) ---
    // Этот Flow автоматически обновляет UI-список при изменении поиска или сортировки
    val filteredBooks: StateFlow<List<Book>> = combine(_allBooks, searchQuery, sortOrder) { books, query, sort ->
        val filtered = if (query.isBlank()) {
            books
        } else {
            books.filter { fuzzyMatch(query, it) }
        }

        when (sort) {
            BookSortOrder.TITLE -> filtered.sortedBy { it.bookName.lowercase() }
            BookSortOrder.AUTHOR -> filtered.sortedBy { it.authorName.lowercase() }
            BookSortOrder.ID -> filtered.sortedBy { it.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val weatherApi = Retrofit.Builder()
        .baseUrl(Constants.WEATHER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)

    init {
        startNetworkMonitoring()
        getAllBooks()
        fetchWeather()
    }

    // --- ЛОГИКА ПОИСКА (Fuzzy) ---
    private fun fuzzyMatch(query: String, book: Book): Boolean {
        val q = query.lowercase().trim()
        val content = "${book.bookName} ${book.authorName} ${book.description}".lowercase()
        if (content.contains(q)) return true
        val words = q.split(" ", ",", ".").filter { it.length > 2 }
        return words.any { content.contains(it) }
    }

    // --- РАБОТА С ПОГОДОЙ ---
    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(city = _currentCity.value)
                _weather.value = "${response.main.temp.toInt()}°C"
            } catch (e: Exception) {
                _weather.value = "Err"
            }
        }
    }

    fun toggleCity() {
        _currentCity.value = if (_currentCity.value == Constants.CITY_MINSK) {
            Constants.CITY_VITEBSK
        } else {
            Constants.CITY_MINSK
        }
        fetchWeather()
    }

    // --- УПРАВЛЕНИЕ РЕПОЗИТОРИЕМ ---
    fun setRepositoryMode(isRemote: Boolean) {
        viewModelScope.launch {
            if (!isRemote && networkHelper.isNetworkAvailable()) {
                _isLoading.value = true
                try {
                    val remoteData = remoteRepo.getAllBooks()
                    localRepo.deleteAllBooks() // Используем твой новый метод
                    remoteData.forEach { localRepo.insertBook(it) }
                } catch (e: Exception) {
                    _errorMessage.value = "Ошибка синхронизации"
                } finally {
                    _isLoading.value = false
                }
            }
            _isRemoteMode.value = isRemote
            currentRepo = if (isRemote) remoteRepo else localRepo
            getAllBooks()
        }
    }

    // --- СЕТЕВОЙ МОНИТОРИНГ ---
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

    // --- CRUD ОПЕРАЦИИ ---
    fun getAllBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allBooks.value = currentRepo.getAllBooks()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertBook(name: String, author: String, desc: String) {
        viewModelScope.launch {
            val book = Book(id = 0, bookName = name, authorName = author, description = desc)
            if (currentRepo.insertBook(book) > 0) getAllBooks()
        }
    }

    fun deleteBook(id: Int) {
        viewModelScope.launch {
            if (currentRepo.deleteBook(id) > 0) getAllBooks()
        }
    }

    fun getBookById(id: Int) {
        viewModelScope.launch {
            _selectedBook.value = currentRepo.getBookById(id)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            if (currentRepo.updateBook(book) > 0) getAllBooks()
        }
    }
}