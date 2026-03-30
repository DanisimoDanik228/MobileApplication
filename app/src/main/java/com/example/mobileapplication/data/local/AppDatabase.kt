package com.example.mobileapplication.data.local

import com.example.mobileapplication.domain.model.Book
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobileapplication.domain.model.BookImage

@Database(entities = [Book::class, BookImage::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun imageDao(): ImageDao
}