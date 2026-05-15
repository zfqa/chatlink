package com.chatapp.domain.repository

import com.chatapp.core.model.FriendRequest
import com.chatapp.core.model.User
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun getPendingRequests(): Flow<List<FriendRequest>>
    suspend fun searchUsers(query: String): List<User>
    suspend fun searchAllUsers(): List<User>
    suspend fun sendRequest(toUserId: String)
    suspend fun acceptRequest(requestId: String)
    suspend fun rejectRequest(requestId: String)
    fun isFriend(userId: String): Boolean
}
