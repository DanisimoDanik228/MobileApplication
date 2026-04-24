package com.example.mobileapplication.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_images")
data class BookImage(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "book_id") // Добавляем колонку для связи с книгой
    var bookId: Int,
    @ColumnInfo(name = "path")
    var path: String
)