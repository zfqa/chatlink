package com.chatapp.data.remote.dto

import com.chatapp.core.model.Conversation
import com.chatapp.core.model.ConversationType
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
    val type: String? = null,
    val peer: UserDto? = null,
    val groupName: String? = null,
    val groupAvatarUrl: String? = null,
    val ownerId: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
) {
    fun toModel(): Conversation {
        val convType = when (type) {
            "GROUP" -> ConversationType.GROUP
            else -> ConversationType.DIRECT
        }
        return Conversation(
            id = id,
            type = convType,
            peer = peer?.toModel() ?: User("", "", "", ""),
            groupName = groupName ?: "",
            groupAvatarUrl = groupAvatarUrl ?: "",
            ownerId = ownerId ?: "",
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount,
            isPinned = isPinned,
            isMuted = isMuted,
        )
    }
}

data class DirectConversationResponse(
    val code: Int,
    val message: String,
    val data: DirectConversationData?,
)

data class DirectConversationData(
    val conversation: ConversationDto,
)

// --- Group ---

data class GroupConversationResponse(
    val code: Int,
    val message: String,
    val data: GroupConversationData?,
)

data class GroupConversationData(
    val conversation: ConversationDto,
)

data class GroupMembersResponse(
    val code: Int,
    val message: String,
    val data: GroupMembersData?,
)

data class GroupMembersData(
    val members: List<GroupMemberDto>,
)

data class GroupMemberDto(
    val id: String,
    val email: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val signature: String? = null,
    val role: String? = null,
) {
    fun toUser() = com.chatapp.core.model.User(
        id = id,
        nickname = nickname ?: "",
        avatarUrl = avatarUrl ?: "",
        signature = signature ?: "",
        email = email ?: "",
    )
}

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
