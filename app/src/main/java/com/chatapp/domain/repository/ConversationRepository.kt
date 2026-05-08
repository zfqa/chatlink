package com.chatapp.domain.repository

import com.chatapp.core.model.Conversation
import com.chatapp.core.model.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, content: String): Message
    fun getOrCreateConversationForPeer(peerId: String): Conversation
}
