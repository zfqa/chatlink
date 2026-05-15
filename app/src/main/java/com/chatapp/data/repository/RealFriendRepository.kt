package com.chatapp.data.repository

import com.chatapp.core.model.FriendRequest
import com.chatapp.core.model.User
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.FriendRequestsResponse
import com.chatapp.data.remote.dto.SendFriendRequestResponse
import com.chatapp.data.remote.dto.UserSearchResponse
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.FriendRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealFriendRepository @Inject constructor(
    private val tokenStore: TokenStore,
    private val contactRepo: ContactRepository,
) : FriendRepository {

    private var version = 0L
    private val refreshSignal = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    private var cachedFriendIds: Set<String> = emptySet()

    override fun getPendingRequests(): Flow<List<FriendRequest>> {
        return refreshSignal.flatMapLatest {
            flow { emit(fetchPendingRequests()) }
        }.onStart {
            emit(fetchPendingRequests())
        }
    }

    private suspend fun fetchPendingRequests(): List<FriendRequest> = withContext(Dispatchers.IO) {
        val token = tokenStore.getAccessToken() ?: return@withContext emptyList()
        try {
            val path = "/friends/requests?status=PENDING"
            android.util.Log.d("FriendRepo", "GET $path token=${token.take(8)}...")
            val raw = NetworkConfig.getJson(path, token)
            val resp = NetworkConfig.parseResponse<FriendRequestsResponse>(raw)
            val list = if (resp.code != 0) emptyList()
                       else resp.data?.requests?.map { it.toModel() } ?: emptyList()
            android.util.Log.d("FriendRepo", "GET $path -> code=${resp.code}, count=${list.size}")
            list
        } catch (e: Exception) {
            android.util.Log.e("FriendRepo", "fetchPendingRequests failed", e)
            emptyList()
        }
    }

    override suspend fun searchUsers(query: String): List<User> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val token = requireToken()
        val path = "/users/search?q=$query"
        android.util.Log.d("FriendRepo", "GET $path token=${token.take(8)}...")
        val raw = NetworkConfig.getJson(path, token)
        val resp = NetworkConfig.parseResponse<UserSearchResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.users?.map { it.toModel() } ?: emptyList()
        android.util.Log.d("FriendRepo", "GET $path -> code=${resp.code}, users=${list.size}")
        list
    }

    override suspend fun searchAllUsers(): List<User> = withContext(Dispatchers.IO) {
        val token = requireToken()
        val path = "/users/search"
        android.util.Log.d("FriendRepo", "GET $path token=${token.take(8)}...")
        val raw = NetworkConfig.getJson(path, token)
        val resp = NetworkConfig.parseResponse<UserSearchResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.users?.map { it.toModel() } ?: emptyList()
        android.util.Log.d("FriendRepo", "GET $path -> code=${resp.code}, users=${list.size}")
        list
    }

    override suspend fun sendRequest(toUserId: String) = withContext(Dispatchers.IO) {
        val token = requireToken()
        val body = mapOf("toUserId" to toUserId)
        android.util.Log.d("FriendRepo", "POST /friends/requests body=$body token=${token.take(8)}...")
        val raw = NetworkConfig.postJson("/friends/requests", body, token)
        val resp = NetworkConfig.parseResponse<SendFriendRequestResponse>(raw)
        android.util.Log.d("FriendRepo", "POST /friends/requests -> code=${resp.code} msg=${resp.message}")
        if (resp.code != 0) throw Exception(resp.message)
    }

    override suspend fun acceptRequest(requestId: String) = withContext(Dispatchers.IO) {
        val token = requireToken()
        val raw = NetworkConfig.putJson("/friends/requests/$requestId/accept", token = token)
        val resp = NetworkConfig.parseResponse<SimpleResponse>(raw)
        if (resp.code != 0) throw Exception(resp.message)
        emitUpdate()
        contactRepo.refreshContacts()
    }

    override suspend fun rejectRequest(requestId: String) = withContext(Dispatchers.IO) {
        val token = requireToken()
        val raw = NetworkConfig.putJson("/friends/requests/$requestId/reject", token = token)
        val resp = NetworkConfig.parseResponse<SimpleResponse>(raw)
        if (resp.code != 0) throw Exception(resp.message)
        emitUpdate()
    }

    override fun isFriend(userId: String): Boolean = cachedFriendIds.contains(userId)

    fun refreshFriendIds(ids: Set<String>) {
        cachedFriendIds = ids
    }

    private fun requireToken(): String {
        return tokenStore.getAccessToken()
            ?: throw ApiException(401, "Not logged in")
    }

    private fun emitUpdate() {
        version++
        refreshSignal.tryEmit(version)
    }

    private data class SimpleResponse(val code: Int, val message: String, val data: Any?)
}
