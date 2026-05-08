package com.chatapp.feature.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Message
import com.chatapp.data.repository.FakeData
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conversationRepo: ConversationRepository,
) : ViewModel() {
    private val conversationId: String = savedStateHandle["conversationId"] ?: ""
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    private val _pendingMessages = MutableStateFlow<List<Message>>(emptyList())
    private val peerName: String
        get() = FakeData.conversations.find { it.id == conversationId }?.peer?.nickname ?: ""

    val uiState: StateFlow<UiState<ChatDetailUiData>> =
        combine(conversationRepo.getMessages(conversationId), _pendingMessages, _inputText) { repoMessages, pending, input ->
            val allMessages = (repoMessages + pending).sortedBy { it.timestamp }
            if (allMessages.isEmpty()) UiState.Empty
            else UiState.Content(ChatDetailUiData(allMessages, peerName, input))
        }.catch { e ->
            emit(UiState.Error(e.message ?: "加载失败"))
        }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = UiState.Loading)

    fun onInputChange(text: String) { _inputText.value = text }
    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        _inputText.value = ""
        viewModelScope.launch {
            try { conversationRepo.sendMessage(conversationId, text) }
            catch (_: Exception) { }
        }
    }
}