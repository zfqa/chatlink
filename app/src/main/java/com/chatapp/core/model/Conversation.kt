package com.chatapp.core.model

data class Conversation(
    val id: String,
    val peer: User,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
)
