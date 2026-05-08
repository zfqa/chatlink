package com.chatapp.feature.profile

import com.chatapp.core.model.User

data class ProfileUiData(
    val user: User,
    val menuItems: List<ProfileMenuItem> = emptyList(),
)

data class ProfileMenuItem(
    val icon: String,
    val title: String,
    val subtitle: String = "",
)
