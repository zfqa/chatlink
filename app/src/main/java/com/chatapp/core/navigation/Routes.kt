package com.chatapp.core.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val CHATS = "chats"
    const val CONTACTS = "contacts"
    const val DISCOVER = "discover"
    const val PROFILE = "profile"
    const val CHAT_DETAIL = "chat_detail/{conversationId}"
    const val CONTACT_DETAIL = "contact_detail/{userId}"
    const val SEARCH_USER = "search_user"
    const val FRIEND_REQUESTS = "friend_requests"
    const val CREATE_GROUP = "create_group"

    fun chatDetail(conversationId: String) = "chat_detail/$conversationId"
    fun contactDetail(userId: String) = "contact_detail/$userId"
}
