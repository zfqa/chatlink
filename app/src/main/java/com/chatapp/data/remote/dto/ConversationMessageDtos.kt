package com.chatapp.data.remote.dto

import com.chatapp.core.model.Conversation
import com.chatapp.core.model.Message
import com.chatapp.core.model.MessageStatus
import com.chatapp.core.model.MessageType

// --- Conversations ---

data class ConversationsResponse(
    val code: Int,
    val message: String,
    val data: ConversationsData?,
)

data class ConversationsData(
    val conversations: List<ConversationDto>,
)

data class ConversationDto(
    val id: String,
    val peer: UserDto,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
) {
    fun toModel(): Conversation = Conversation(
        id = id,
        peer = peer.toModel(),
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
    )
}

data class DirectConversationResponse(
    val code: Int,
    val message: String,
    val data: DirectConversationData?,
)

data class DirectConversationData(
    val conversation: ConversationDto,
)

// --- Messages ---

data class MessagesResponse(
    val code: Int,
    val message: String,
    val data: MessagesData?,
)

data class MessagesData(
    val messages: List<MessageDto>,
    val hasMore: Boolean = false,
)

data class MessageDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val type: String = "TEXT",
    val timestamp: Long,
    val status: String = "SENT",
) {
    fun toModel(): Message = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        type = when (type) {
            "IMAGE" -> MessageType.IMAGE
            else -> MessageType.TEXT
        },
        timestamp = timestamp,
        status = when (status) {
            "SENDING" -> MessageStatus.SENDING
            "FAILED" -> MessageStatus.FAILED
            else -> MessageStatus.SENT
        },
    )
}

data class SendMessageResponse(
    val code: Int,
    val message: String,
    val data: SendMessageData?,
)

data class SendMessageData(
    val message: MessageDto,
)
