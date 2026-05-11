package com.chatapp.feature.searchuser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.User
import com.chatapp.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUserUiData(
    val results: List<User> = emptyList(),
    val friendIds: Set<String> = emptySet(),
    val pendingIds: Set<String> = emptySet(),
    val query: String = "",
)

@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val friendRepo: FriendRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SearchUserUiData>>(UiState.Content(SearchUserUiData()))
    val uiState: StateFlow<UiState<SearchUserUiData>> = _uiState.asStateFlow()

    private val _pendingIds = mutableSetOf<String>()

    init {
        loadAllUsers("")
    }

    private fun loadAllUsers(query: String) {
        val allResults = if (query.isBlank()) friendRepo.searchAllUsers()
                         else friendRepo.searchUsers(query)
        val friendIds = allResults.filter { friendRepo.isFriend(it.id) }.map { it.id }.toSet()
        _uiState.value = UiState.Content(
            SearchUserUiData(results = allResults, friendIds = friendIds, pendingIds = _pendingIds.toSet(), query = query)
        )
    }

    fun onQueryChange(query: String) {
        loadAllUsers(query)
    }

    fun sendRequest(userId: String) {
        viewModelScope.launch {
            friendRepo.sendRequest(userId)
            _pendingIds.add(userId)
            val current = _uiState.value
            if (current is UiState.Content) {
                _uiState.value = current.copy(data = current.data.copy(pendingIds = _pendingIds.toSet()))
            }
        }
    }
}
