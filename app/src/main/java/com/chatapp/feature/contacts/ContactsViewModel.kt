package com.chatapp.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.repository.RealContactRepository
import com.chatapp.data.repository.RealFriendRepository
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepo: ContactRepository,
    private val friendRepo: FriendRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<ContactsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContactsUiData>> = _uiState.asStateFlow()

    private val _pendingRequestCount = MutableStateFlow(0)
    val pendingRequestCount: StateFlow<Int> = _pendingRequestCount.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            // Fetch contacts from backend, then observe
            flow {
                if (contactRepo is RealContactRepository) {
                    contactRepo.fetchContacts()
                }
                emitAll(combine(contactRepo.getContacts(), _query) { contacts, q ->
                    if (contacts.isEmpty()) UiState.Empty
                    else UiState.Content(ContactsUiData(contacts, q))
                })
            }.catch { e ->
                if (e is ApiException && e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
                emit(UiState.Error(e.message ?: "Load failed"))
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            friendRepo.getPendingRequests().collect { list ->
                _pendingRequestCount.value = list.size
                if (friendRepo is RealFriendRepository) {
                    val contacts = contactRepo.getContacts().first()
                    friendRepo.refreshFriendIds(contacts.map { it.user.id }.toSet())
                }
            }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }
}
