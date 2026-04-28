package com.example.mobileapplication.domain.model

data class Book(
    val id: Long = 0L,
    val bookName: String,
    val description: String,
    val authorName: String,
    val imageUrl: String? = null,
    val imageFileId: String? = null,
    /** Mirrors EV Scenario.temperature in Firestore. */
    val temperature: Int = 22
)
