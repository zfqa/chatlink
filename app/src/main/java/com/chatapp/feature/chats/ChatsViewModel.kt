package com.chatapp.feature.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.repository.RealConversationRepository
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<ChatsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ChatsUiData>> = _uiState.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            flow {
                if (conversationRepo is RealConversationRepository) {
                    conversationRepo.fetchConversations()
                }
                emitAll(combine(
                    conversationRepo.getConversations(),
                    _query,
                ) { conversations, query ->
                    if (conversations.isEmpty()) UiState.Empty
                    else UiState.Content(ChatsUiData(conversations, query))
                })
            }.catch { e ->
                if (e is ApiException && e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
                emit(UiState.Error(e.message ?: "Load failed"))
            }.collect { _uiState.value = it }
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
