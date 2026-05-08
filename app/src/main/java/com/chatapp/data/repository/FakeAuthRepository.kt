package com.chatapp.data.repository

import com.chatapp.core.model.User
import com.chatapp.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): User {
        delay(800)
        val user = User(
            id = "u_" + email.hashCode(),
            nickname = email.substringBefore("@"),
            avatarUrl = "",
            signature = "",
        )
        currentUser = user
        return user
    }

    override suspend fun register(email: String, password: String, nickname: String): User {
        delay(800)
        val user = User(
            id = "u_" + email.hashCode(),
            nickname = nickname.ifBlank { email.substringBefore("@") },
            avatarUrl = "",
            signature = "",
        )
        currentUser = user
        return user
    }

    override suspend fun logout() {
        currentUser = null
    }

    override fun isLoggedIn(): Boolean = currentUser != null
}
