package com.chatapp.data.remote.dto

import com.chatapp.core.model.Contact
import com.chatapp.core.model.FriendRequest
import com.chatapp.core.model.FriendRequestStatus
import com.chatapp.core.model.User

// --- Contacts ---

data class ContactsResponse(
    val code: Int,
    val message: String,
    val data: ContactsData?,
)

data class ContactsData(
    val contacts: List<ContactDto>,
)

data class ContactDto(
    val user: UserDto,
    val isOnline: Boolean = false,
    val remark: String = "",
    val pinyinInitial: String = "",
) {
    fun toModel(): Contact = Contact(
        user = user.toModel(),
        isOnline = isOnline,
        remark = remark,
        pinyinInitial = pinyinInitial.ifBlank { user.nickname.firstOrNull()?.uppercase() ?: "#" },
    )
}

// --- Friend Requests ---

data class FriendRequestsResponse(
    val code: Int,
    val message: String,
    val data: FriendRequestsData?,
)

data class FriendRequestsData(
    val requests: List<FriendRequestDto>,
)

data class FriendRequestDto(
    val id: String,
    val fromUser: UserDto,
    val toUserId: String,
    val timestamp: Long,
    val status: String,
) {
    fun toModel(): FriendRequest = FriendRequest(
        id = id,
        fromUser = fromUser.toModel(),
        toUserId = toUserId,
        timestamp = timestamp,
        status = when (status) {
            "ACCEPTED" -> FriendRequestStatus.ACCEPTED
            "REJECTED" -> FriendRequestStatus.REJECTED
            else -> FriendRequestStatus.PENDING
        },
    )
}

data class SendFriendRequestResponse(
    val code: Int,
    val message: String,
    val data: SendFriendRequestData?,
)

data class SendFriendRequestData(
    val request: FriendRequestDto,
)

// --- User Search ---

data class UserSearchResponse(
    val code: Int,
    val message: String,
    val data: UserSearchData?,
)

data class UserSearchData(
    val users: List<UserDto>,
)
