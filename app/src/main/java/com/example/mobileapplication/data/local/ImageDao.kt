package com.example.mobileapplication.data.local

import androidx.room.*
import com.example.mobileapplication.domain.model.BookImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: BookImage)

    @Query("SELECT * FROM book_images")
    fun getAllImages(): List<BookImage>

    @Update
    suspend fun updateImage(image: BookImage)

    @Delete
    suspend fun deleteImage(image: BookImage)
}