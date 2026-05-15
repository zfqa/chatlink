package com.chatapp.feature.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Message
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.repository.RealConversationRepository
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepo: ConversationRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {
    private val conversationId: String = savedStateHandle["conversationId"] ?: ""
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    val currentUserId: String = tokenStore.getUserId() ?: ""

    private val peerName = MutableStateFlow("")

    val uiState: StateFlow<UiState<ChatDetailUiData>> =
        combine(conversationRepo.getMessages(conversationId), peerName) { messages, name ->
            if (messages.isEmpty()) UiState.Empty
            else UiState.Content(ChatDetailUiData(messages, name))
        }.catch { e ->
            emit(UiState.Error(e.message ?: "Load failed"))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    init {
        // Fetch messages from backend
        viewModelScope.launch {
            try {
                if (conversationRepo is RealConversationRepository) {
                    conversationRepo.fetchMessages(conversationId)
                    // Try to get peer name from conversations list
                    val conv = conversationRepo.getConversations().first().find { it.id == conversationId }
                    if (conv != null) peerName.value = conv.peer.nickname
                }
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
            } catch (_: Exception) {}
        }
    }

    fun onInputChange(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        _inputText.value = ""
        _sendError.value = null
        viewModelScope.launch {
            try {
                conversationRepo.sendMessage(conversationId, text)
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                } else {
                    _sendError.value = e.message ?: "Send failed"
                }
            } catch (e: Exception) {
                _sendError.value = e.message ?: "Network error"
            }
        }
    }
}
