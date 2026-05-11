package com.chatapp.data.repository

import com.chatapp.core.model.FriendRequest
import com.chatapp.core.model.FriendRequestStatus
import com.chatapp.core.model.User
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.FriendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFriendRepository @Inject constructor(
    private val contactRepo: ContactRepository,
) : FriendRepository {

    private var version = 0L
    private val refreshSignal = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    override fun getPendingRequests(): Flow<List<FriendRequest>> {
        return refreshSignal.flatMapLatest {
            val filtered = FakeData.friendRequests.filter {
                it.status == FriendRequestStatus.PENDING && it.toUserId == FakeData.me.id
            }
            flowOf(filtered)
        }.onStart {
            val filtered = FakeData.friendRequests.filter {
                it.status == FriendRequestStatus.PENDING && it.toUserId == FakeData.me.id
            }
            emit(filtered)
        }
    }

    override fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return FakeData.allUsers.filter {
            it.nickname.lowercase().contains(q) || it.id.lowercase().contains(q)
        }
    }

    override fun searchAllUsers(): List<User> = FakeData.allUsers

    override suspend fun sendRequest(toUserId: String) {
        val existing = FakeData.friendRequests.find {
            it.fromUser.id == FakeData.me.id && it.toUserId == toUserId && it.status == FriendRequestStatus.PENDING
        }
        if (existing != null) return
        FakeData.addFriendRequest(FakeData.me, toUserId)
        emitUpdate()
    }

    override suspend fun acceptRequest(requestId: String) {
        FakeData.acceptFriendRequest(requestId)
        emitUpdate()
        contactRepo.refreshContacts()
    }

    override suspend fun rejectRequest(requestId: String) {
        FakeData.rejectFriendRequest(requestId)
        emitUpdate()
    }

    override fun isFriend(userId: String): Boolean = FakeData.friendIds.contains(userId)

    private fun emitUpdate() {
        version++
        refreshSignal.tryEmit(version)
    }
}
