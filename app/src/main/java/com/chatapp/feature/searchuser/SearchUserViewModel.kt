package com.chatapp.feature.searchuser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.User
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUserUiData(
    val results: List<User> = emptyList(),
    val friendIds: Set<String> = emptySet(),
    val pendingIds: Set<String> = emptySet(),
)

@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val friendRepo: FriendRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<SearchUserUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<SearchUserUiData>> = _uiState.asStateFlow()

    private val _pendingIds = mutableSetOf<String>()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAllUsers("")
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            loadAllUsers(newQuery)
        }
    }

    private fun loadAllUsers(query: String) {
        viewModelScope.launch {
            try {
                val results = if (query.isBlank()) friendRepo.searchAllUsers()
                              else friendRepo.searchUsers(query)
                val friendIds = results.filter { friendRepo.isFriend(it.id) }.map { it.id }.toSet()
                _uiState.value = UiState.Content(
                    SearchUserUiData(results = results, friendIds = friendIds, pendingIds = _pendingIds.toSet())
                )
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                } else {
                    _uiState.value = UiState.Error(e.message ?: "Search failed")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun sendRequest(userId: String) {
        viewModelScope.launch {
            try {
                friendRepo.sendRequest(userId)
                _pendingIds.add(userId)
                val current = _uiState.value
                if (current is UiState.Content) {
                    _uiState.value = current.copy(data = current.data.copy(pendingIds = _pendingIds.toSet()))
                }
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
            } catch (_: Exception) {}
        }
    }
}
