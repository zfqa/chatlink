package com.chatapp.data.repository

import android.util.Log
import com.chatapp.core.model.Conversation
import com.chatapp.core.model.Message
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.ConversationsResponse
import com.chatapp.data.remote.dto.DirectConversationResponse
import com.chatapp.data.remote.dto.GroupConversationResponse
import com.chatapp.data.remote.dto.GroupMembersResponse
import com.chatapp.data.remote.dto.MessagesResponse
import com.chatapp.data.remote.dto.SendMessageResponse
import com.chatapp.domain.repository.ConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealConversationRepository @Inject constructor(
    private val tokenStore: TokenStore,
) : ConversationRepository {

    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    private val messagesFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()
    private val recentSentIds = LinkedHashSet<String>() // track recently sent message IDs for dedup
    @Volatile var activeConversationId: String? = null // currently viewed conversation
    private companion object {
        const val TAG = "ConvRepo"
        const val MAX_SENT_IDS = 100
    }

    override fun getConversations(): Flow<List<Conversation>> = conversationsFlow

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    }

    suspend fun fetchConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val token = requireToken()
        Log.d(TAG, "GET /conversations")
        val raw = NetworkConfig.getJson("/conversations", token)
        val resp = NetworkConfig.parseResponse<ConversationsResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.conversations?.map { it.toModel() } ?: emptyList()
        Log.d(TAG, "GET /conversations -> count=${list.size}")
        conversationsFlow.value = list
        list
    }

    suspend fun fetchMessages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        val token = requireToken()
        Log.d(TAG, "GET /conversations/$conversationId/messages")
        val raw = NetworkConfig.getJson("/conversations/$conversationId/messages", token)
        val resp = NetworkConfig.parseResponse<MessagesResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.messages?.map { it.toModel() } ?: emptyList()
        Log.d(TAG, "GET messages -> count=${list.size}")
        val flow = messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        flow.value = list
        list
    }

    suspend fun markAsRead(conversationId: String) = withContext(Dispatchers.IO) {
        activeConversationId = conversationId
        val token = requireToken()
        try {
            NetworkConfig.postJson("/conversations/$conversationId/read", emptyMap<String, String>(), token)
            // Update local unread count to 0
            conversationsFlow.value = conversationsFlow.value.map { conv ->
                if (conv.id == conversationId) conv.copy(unreadCount = 0) else conv
            }
            Log.d(TAG, "markAsRead conv=$conversationId")
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead failed: ${e.message}")
        }
    }

    fun clearActiveConversation() {
        activeConversationId = null
    }

    fun clearAllState() {
        conversationsFlow.value = emptyList()
        messagesFlows.clear()
        recentSentIds.clear()
        activeConversationId = null
    }

    override suspend fun sendMessage(conversationId: String, content: String): Message = withContext(Dispatchers.IO) {
        val token = requireToken()
        val body = mapOf("content" to content, "type" to "TEXT")
        Log.d(TAG, "POST /conversations/$conversationId/messages")
        val raw = NetworkConfig.postJson("/conversations/$conversationId/messages", body, token)
        val resp = NetworkConfig.parseResponse<SendMessageResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        val msg = resp.data.message.toModel()
        Log.d(TAG, "POST message -> msgId=${msg.id}")

        // Track sent ID so WS push for same message is ignored
        synchronized(recentSentIds) {
            recentSentIds.add(msg.id)
            if (recentSentIds.size > MAX_SENT_IDS) {
                val iter = recentSentIds.iterator()
                iter.next()
                iter.remove()
            }
        }

        // Append to local messages flow
        val flow = messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        if (flow.value.none { it.id == msg.id }) {
            flow.value = flow.value + msg
        }
        // Update conversation list
        updateConversationLastMessage(conversationId, content, msg.timestamp)
        msg
    }

    override suspend fun getOrCreateConversationForPeer(peerId: String): Conversation = withContext(Dispatchers.IO) {
        val token = requireToken()
        Log.d(TAG, "POST /conversations/direct peerId=$peerId")
        val raw = NetworkConfig.postJson("/conversations/direct", mapOf("peerId" to peerId), token)
        val resp = NetworkConfig.parseResponse<DirectConversationResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        val conv = resp.data.conversation.toModel()
        Log.d(TAG, "POST /conversations/direct -> convId=${conv.id}")
        val current = conversationsFlow.value.toMutableList()
        val existing = current.indexOfFirst { it.id == conv.id }
        if (existing >= 0) current[existing] = conv else current.add(0, conv)
        conversationsFlow.value = current
        conv
    }

    suspend fun createGroup(name: String, memberIds: List<String>): Conversation = withContext(Dispatchers.IO) {
        val token = requireToken()
        val body = mapOf("name" to name, "memberIds" to memberIds)
        Log.d(TAG, "POST /conversations/group name=$name members=${memberIds.size}")
        val raw = NetworkConfig.postJson("/conversations/group", body, token)
        val resp = NetworkConfig.parseResponse<GroupConversationResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        val conv = resp.data.conversation.toModel()
        Log.d(TAG, "POST /conversations/group -> convId=${conv.id}")
        val current = conversationsFlow.value.toMutableList()
        current.add(0, conv)
        conversationsFlow.value = current
        conv
    }

    suspend fun getGroupMembers(conversationId: String): List<com.chatapp.core.model.User> = withContext(Dispatchers.IO) {
        val token = requireToken()
        val raw = NetworkConfig.getJson("/conversations/$conversationId/members", token)
        val resp = NetworkConfig.parseResponse<GroupMembersResponse>(raw)
        if (resp.code != 0) return@withContext emptyList()
        resp.data?.members?.map { it.toUser() } ?: emptyList()
    }

    fun getCurrentUserId(): String? = tokenStore.getUserId()

    /**
     * Called by WebSocketManager when a new message arrives.
     * Returns true if the message is for a non-active conversation (caller should notify).
     */
    fun onIncomingMessage(message: Message): Boolean {
        // Skip if this message was sent by us via HTTP (already in the list)
        synchronized(recentSentIds) {
            if (recentSentIds.contains(message.id)) {
                Log.d(TAG, "WS skip: msgId=${message.id} (recently sent via HTTP)")
                return false
            }
        }

        val flow = messagesFlows.getOrPut(message.conversationId) { MutableStateFlow(emptyList()) }
        // Deduplicate against existing messages
        if (flow.value.any { it.id == message.id }) {
            Log.d(TAG, "WS skip: msgId=${message.id} (already in list)")
            return false
        }

        flow.value = flow.value + message
        updateConversationLastMessage(message.conversationId, message.content, message.timestamp)

        // Check if user is currently viewing this conversation
        val isActive = message.conversationId == activeConversationId
        if (!isActive) {
            // Increment unread count
            conversationsFlow.value = conversationsFlow.value.map { conv ->
                if (conv.id == message.conversationId) conv.copy(unreadCount = conv.unreadCount + 1) else conv
            }
            Log.d(TAG, "WS unread++: conv=${message.conversationId}")
        } else {
            Log.d(TAG, "WS active chat: conv=${message.conversationId}")
        }
        return !isActive
    }

    private fun updateConversationLastMessage(conversationId: String, content: String, timestamp: Long) {
        val current = conversationsFlow.value.toMutableList()
        val idx = current.indexOfFirst { it.id == conversationId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(lastMessage = content, lastMessageTime = timestamp)
        }
        conversationsFlow.value = current.sortedByDescending { it.lastMessageTime }
    }

    private fun requireToken(): String {
        return tokenStore.getAccessToken()
            ?: throw ApiException(401, "Not logged in")
    }
}
