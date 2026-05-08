package com.chatapp.domain.repository

import com.chatapp.core.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(email: String, password: String, nickname: String): User
    suspend fun logout()
    fun isLoggedIn(): Boolean
}