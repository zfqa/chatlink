package com.chatapp.core.model

enum class MessageType { TEXT, IMAGE }
enum class MessageStatus { SENDING, SENT, FAILED }

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT,
)
