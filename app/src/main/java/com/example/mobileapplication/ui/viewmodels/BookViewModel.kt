package com.example.mobileapplication.ui.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.core.AlarmReceiver
import com.example.mobileapplication.core.Constants
import com.example.mobileapplication.core.NetworkHelper
import com.example.mobileapplication.data.remote.WeatherApiService
import com.example.mobileapplication.data.repository.BookFirestoreRepository
import com.example.mobileapplication.domain.model.Book
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class BookViewModel(
    private val networkHelper: NetworkHelper,
    private val bookRepo: BookFirestoreRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _ping = MutableStateFlow("-")
    val ping = _ping.asStateFlow()

    private val _currentCity = MutableStateFlow(Constants.CITY_WEATHER)
    val currentCity = _currentCity.asStateFlow()

    private val _weather = MutableStateFlow("..°C")
    val weather = _weather.asStateFlow()

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(BookSortOrder.TITLE)

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook = _selectedBook.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        sharedPreferences.getBoolean("notifications_active", false)
    )
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

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

    private var booksJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { fa ->
        booksJob?.cancel()
        if (fa.currentUser != null) {
            booksJob = viewModelScope.launch {
                bookRepo.observeBooks()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Firestore error"
                        _allBooks.value = emptyList()
                    }
                    .collect { list -> _allBooks.value = list }
            }
        } else {
            _allBooks.value = emptyList()
        }
    }

    init {
        startNetworkMonitoring()
        fetchWeather()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
        super.onCleared()
    }

    private fun fuzzyMatch(query: String, book: Book): Boolean {
        val q = query.lowercase().trim()
        val content = "${book.bookName} ${book.authorName} ${book.description}".lowercase()
        if (content.contains(q)) return true
        val words = q.split(" ", ",", ".").filter { it.length > 2 }
        return words.any { content.contains(it) }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(city = _currentCity.value)
                _weather.value = "${response.main.temp.toInt()}°C"
            } catch (_: Exception) {
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
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        _notificationsEnabled.value = true
    }

    fun startMinuteNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5000

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

    fun refreshBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allBooks.value = bookRepo.getAllBooks()
            } catch (_: Exception) {
                _errorMessage.value = "Ошибка загрузки данных"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertBook(name: String, author: String, desc: String) {
        viewModelScope.launch {
            try {
                val id = System.currentTimeMillis()
                val book = Book(
                    id = id,
                    bookName = name,
                    authorName = author,
                    description = desc
                )
                bookRepo.saveBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка сохранения"
            }
        }
    }

    fun setBookCoverFromUri(bookId: Long, uriString: String) {
        viewModelScope.launch {
            try {
                val current = bookRepo.getBookById(bookId) ?: return@launch
                val updated = current.copy(imageUrl = uriString)
                bookRepo.updateBook(updated)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки изображения"
            }
        }
    }

    fun deleteBook(id: Long) {
        viewModelScope.launch {
            try {
                bookRepo.deleteBook(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка удаления"
            }
        }
    }

    fun getBookById(id: Long) {
        viewModelScope.launch {
            _selectedBook.value = bookRepo.getBookById(id)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            try {
                bookRepo.updateBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка обновления"
            }
        }
    }

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
}
