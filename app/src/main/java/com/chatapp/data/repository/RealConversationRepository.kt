package com.chatapp.data.repository

import com.chatapp.core.model.Conversation
import com.chatapp.core.model.Message
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.ConversationsResponse
import com.chatapp.data.remote.dto.DirectConversationResponse
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

    override fun getConversations(): Flow<List<Conversation>> = conversationsFlow

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
    }

    suspend fun fetchConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val token = requireToken()
        android.util.Log.d("ConvRepo", "GET /conversations token=${token.take(8)}...")
        val raw = NetworkConfig.getJson("/conversations", token)
        val resp = NetworkConfig.parseResponse<ConversationsResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.conversations?.map { it.toModel() } ?: emptyList()
        android.util.Log.d("ConvRepo", "GET /conversations -> code=${resp.code}, count=${list.size}")
        conversationsFlow.value = list
        list
    }

    suspend fun fetchMessages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        val token = requireToken()
        val path = "/conversations/$conversationId/messages"
        android.util.Log.d("ConvRepo", "GET $path token=${token.take(8)}...")
        val raw = NetworkConfig.getJson(path, token)
        val resp = NetworkConfig.parseResponse<MessagesResponse>(raw)
        val list = if (resp.code != 0) emptyList()
                   else resp.data?.messages?.map { it.toModel() } ?: emptyList()
        android.util.Log.d("ConvRepo", "GET $path -> code=${resp.code}, count=${list.size}")
        val flow = messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        flow.value = list
        list
    }

    override suspend fun sendMessage(conversationId: String, content: String): Message = withContext(Dispatchers.IO) {
        val token = requireToken()
        val path = "/conversations/$conversationId/messages"
        val body = mapOf("content" to content, "type" to "TEXT")
        android.util.Log.d("ConvRepo", "POST $path body=$body token=${token.take(8)}...")
        val raw = NetworkConfig.postJson(path, body, token)
        val resp = NetworkConfig.parseResponse<SendMessageResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        val msg = resp.data.message.toModel()
        android.util.Log.d("ConvRepo", "POST $path -> code=${resp.code}, msgId=${msg.id}")
        // Append to local messages flow
        val flow = messagesFlows.getOrPut(conversationId) { MutableStateFlow(emptyList()) }
        flow.value = flow.value + msg
        // Update conversation list
        updateConversationLastMessage(conversationId, content, msg.timestamp)
        msg
    }

    override suspend fun getOrCreateConversationForPeer(peerId: String): Conversation = withContext(Dispatchers.IO) {
        val token = requireToken()
        val body = mapOf("peerId" to peerId)
        android.util.Log.d("ConvRepo", "POST /conversations/direct body=$body token=${token.take(8)}...")
        val raw = NetworkConfig.postJson("/conversations/direct", body, token)
        val resp = NetworkConfig.parseResponse<DirectConversationResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        val conv = resp.data.conversation.toModel()
        android.util.Log.d("ConvRepo", "POST /conversations/direct -> code=${resp.code}, convId=${conv.id}")
        // Update conversations list if this is a new conversation
        val current = conversationsFlow.value.toMutableList()
        val existing = current.indexOfFirst { it.id == conv.id }
        if (existing >= 0) {
            current[existing] = conv
        } else {
            current.add(0, conv)
        }
        conversationsFlow.value = current
        conv
    }

    fun getCurrentUserId(): String? = tokenStore.getUserId()

    private fun updateConversationLastMessage(conversationId: String, content: String, timestamp: Long) {
        val current = conversationsFlow.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(lastMessage = content, lastMessageTime = timestamp)
            } else conv
        }.sortedByDescending { it.lastMessageTime }
        conversationsFlow.value = current
    }

    private fun requireToken(): String {
        return tokenStore.getAccessToken()
            ?: throw ApiException(401, "Not logged in")
    }
}
