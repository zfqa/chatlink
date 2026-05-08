package com.chatapp.feature.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<ChatsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ChatsUiData>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                conversationRepo.getConversations(),
                _query,
            ) { conversations, query ->
                if (conversations.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Content(ChatsUiData(conversations, query))
                }
            }.catch { e ->
                emit(UiState.Error(e.message ?: "加载失败"))
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
