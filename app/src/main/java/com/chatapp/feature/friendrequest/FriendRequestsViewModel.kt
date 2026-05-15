package com.chatapp.feature.friendrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.FriendRequest
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendRequestsUiData(
    val requests: List<FriendRequest>,
)

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    private val friendRepo: FriendRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FriendRequestsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<FriendRequestsUiData>> = _uiState.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            friendRepo.getPendingRequests().collect { list ->
                _uiState.value = if (list.isEmpty()) UiState.Empty
                else UiState.Content(FriendRequestsUiData(list))
            }
        }
    }

    fun accept(requestId: String) {
        viewModelScope.launch {
            try {
                friendRepo.acceptRequest(requestId)
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
            } catch (_: Exception) {}
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            try {
                friendRepo.rejectRequest(requestId)
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
            } catch (_: Exception) {}
        }
    }
}
