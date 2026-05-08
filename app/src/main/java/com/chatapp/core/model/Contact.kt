package com.chatapp.core.model

data class Contact(
    val user: User,
    val isOnline: Boolean = false,
    val pinyinInitial: String = "",
    val remark: String = "",
)
