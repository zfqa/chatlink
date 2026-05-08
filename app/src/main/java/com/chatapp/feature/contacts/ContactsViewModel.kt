package com.chatapp.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepo: ContactRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow<UiState<ContactsUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContactsUiData>> = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            combine(contactRepo.getContacts(), _query) { contacts, q ->
                if (contacts.isEmpty()) UiState.Empty
                else UiState.Content(ContactsUiData(contacts, q))
            }.catch { e ->
                emit(UiState.Error(e.message ?: "加载失败"))
            }.collect { _uiState.value = it }
        }
    }
    fun onQueryChange(q: String) { _query.value = q }
}
