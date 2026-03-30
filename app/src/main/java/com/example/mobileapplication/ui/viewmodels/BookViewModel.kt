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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mobileapplication.core.AlarmReceiver
import java.util.*

class BookViewModel(
    private val networkHelper: NetworkHelper,
    private val remoteRepo: BookRepository,
    private val localRepo: BookRepository,
    private val sharedPreferences: android.content.SharedPreferences
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

    private val _notificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("notifications_active", false)
    )
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    fun scheduleNotification(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // Если время уже прошло сегодня, ставим на завтра
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // Устанавливаем повторяющееся уведомление каждый день
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        _notificationsEnabled.value = true
    }

    // В BookViewModel.kt
    fun startMinuteNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5000 // Первый запуск через 5 секунд

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        sharedPreferences.edit().putBoolean("notifications_active", true).apply()
        _notificationsEnabled.value = true
    }

    fun stopNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        sharedPreferences.edit().putBoolean("notifications_active", false).apply()
        _notificationsEnabled.value = false
    }
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