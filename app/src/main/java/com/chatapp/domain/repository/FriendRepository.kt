package com.chatapp.domain.repository

import com.chatapp.core.model.FriendRequest
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    fun getPendingRequests(): Flow<List<FriendRequest>>
    fun searchUsers(query: String): List<com.chatapp.core.model.User>
    fun searchAllUsers(): List<com.chatapp.core.model.User>
    suspend fun sendRequest(toUserId: String)
    suspend fun acceptRequest(requestId: String)
    suspend fun rejectRequest(requestId: String)
    fun isFriend(userId: String): Boolean
}
