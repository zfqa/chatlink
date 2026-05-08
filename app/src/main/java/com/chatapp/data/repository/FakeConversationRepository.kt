package com.chatapp.data.repository

import com.chatapp.core.model.Conversation
import com.chatapp.core.model.Message
import com.chatapp.domain.repository.ConversationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeConversationRepository @Inject constructor() : ConversationRepository {

    private val conversationsFlow = MutableStateFlow(FakeData.conversations)

    override fun getConversations(): Flow<List<Conversation>> = conversationsFlow

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return conversationsFlow.map { _ ->
            FakeData.messagesMap[conversationId]?.toList() ?: emptyList()
        }
    }

    override suspend fun sendMessage(conversationId: String, content: String): Message {
        // Simulate network delay
        delay(300)
        val msg = FakeData.addMessage(conversationId, content)
        // Trigger flow re-emission by updating the list
        conversationsFlow.value = FakeData.conversations.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(lastMessage = content, lastMessageTime = msg.timestamp)
            } else conv
        }
        return msg
    }
}
