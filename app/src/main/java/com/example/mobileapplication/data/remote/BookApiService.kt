package com.example.mobileapplication.data.remote

import com.example.mobileapplication.domain.model.Book
import retrofit2.Response
import retrofit2.http.*

interface BookApiService {

    @GET("api/books")
    suspend fun getBooks(): List<Book>

    @GET("api/books/{id}")
    suspend fun getBook(@Path("id") id: Int): Response<Book>

    @POST("api/books")
    suspend fun postBook(@Body book: Book): Book

    @PUT("api/books/{id}")
    suspend fun putBook(@Path("id") id: Int, @Body book: Book): Response<Unit>

    @DELETE("api/books/{id}")
    suspend fun deleteBook(@Path("id") id: Int): Response<Unit>
}