package com.example.mobileapplication.Repository

interface UserRepository {
    fun getUserById(id: Int): User?
    fun saveUser(user: User)
    fun getAllUsers() : List<User>

    fun addUser(user: User)
}