package com.chatapp.feature.chats

import com.chatapp.core.model.Conversation

/** Wraps all possible states for the Chats screen. */
data class ChatsUiData(
    val conversations: List<Conversation>,
    val filterQuery: String = "",
) {
    val filtered: List<Conversation>
        get() = if (filterQuery.isBlank()) conversations
                else conversations.filter {
                    it.displayName.contains(filterQuery, ignoreCase = true) ||
                    it.lastMessage.contains(filterQuery, ignoreCase = true)
                }
}
