package com.chatapp.core.model

enum class ConversationType { DIRECT, GROUP }

data class Conversation(
    val id: String,
    val type: ConversationType = ConversationType.DIRECT,
    val peer: User = User("", "", "", ""),
    val groupName: String = "",
    val groupAvatarUrl: String = "",
    val ownerId: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
) {
    val displayName: String
        get() = if (type == ConversationType.GROUP) groupName else peer.nickname

    val displayAvatarUrl: String
        get() = if (type == ConversationType.GROUP) groupAvatarUrl else peer.avatarUrl
}
