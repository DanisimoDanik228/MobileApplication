package com.example.mobileapplication.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "book_name")
    var bookName: String,
    @ColumnInfo(name = "description")
    val description : String,
    @ColumnInfo(name = "author_name")
    val authorName : String
)