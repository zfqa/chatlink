package com.chatapp.data.remote.dto

import com.chatapp.core.model.User

// --- Request DTOs ---

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
)

// --- Response DTOs ---

data class AuthResponse(
    val code: Int,
    val message: String,
    val data: AuthData?,
)

data class AuthData(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

data class UserDto(
    val id: String,
    val nickname: String,
    val avatarUrl: String,
    val signature: String,
) {
    fun toModel(): User = User(id, nickname, avatarUrl, signature)
}

data class UsersSearchResponse(
    val code: Int,
    val message: String,
    val data: UsersSearchData?,
)

data class UsersSearchData(
    val users: List<UserDto>,
)

data class CurrentUserResponse(
    val code: Int,
    val message: String,
    val data: UserDto?,
)
