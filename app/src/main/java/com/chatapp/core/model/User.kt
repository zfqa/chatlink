package com.chatapp.core.model

data class User(
    val id: String,
    val nickname: String,
    val avatarUrl: String,
    val signature: String = "",
    val email: String = "",
)
