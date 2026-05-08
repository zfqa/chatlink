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

    fun chatDetail(conversationId: String) = "chat_detail/$conversationId"
}
