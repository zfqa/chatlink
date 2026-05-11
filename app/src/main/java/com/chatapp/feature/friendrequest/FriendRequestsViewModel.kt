package com.chatapp.feature.friendrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.FriendRequest
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FriendRequestsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<FriendRequestsUiData>> = _uiState.asStateFlow()

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
            friendRepo.acceptRequest(requestId)
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            friendRepo.rejectRequest(requestId)
        }
    }
}
