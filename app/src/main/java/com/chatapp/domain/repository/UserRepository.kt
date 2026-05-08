package com.chatapp.domain.repository

import com.chatapp.core.model.User

interface UserRepository {
    suspend fun getCurrentUser(): User
}
