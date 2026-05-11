package com.chatapp.core.model

data class FriendRequest(
    val id: String,
    val fromUser: User,
    val toUserId: String,
    val timestamp: Long,
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
)

enum class FriendRequestStatus { PENDING, ACCEPTED, REJECTED }
