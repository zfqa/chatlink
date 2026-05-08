package com.chatapp.feature.contactdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Contact
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailUiData(
    val contact: Contact,
    val conversationId: String = "",
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactRepo: ContactRepository,
    private val conversationRepo: ConversationRepository,
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    private val _uiState = MutableStateFlow<UiState<ContactDetailUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ContactDetailUiData>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val contact = contactRepo.getContact(userId)
                if (contact == null) {
                    _uiState.value = UiState.Error("Contact not found")
                } else {
                    val conv = conversationRepo.getOrCreateConversationForPeer(userId)
                    _uiState.value = UiState.Content(ContactDetailUiData(contact, conv.id))
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Load failed")
            }
        }
    }
}
