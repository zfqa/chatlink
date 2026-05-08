package com.chatapp.feature.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    CHATS("chats", "聊天", Icons.Default.Chat),
    CONTACTS("contacts", "联系人", Icons.Default.Contacts),
    DISCOVER("discover", "发现", Icons.Default.Explore),
    PROFILE("profile", "我", Icons.Default.Person),
}