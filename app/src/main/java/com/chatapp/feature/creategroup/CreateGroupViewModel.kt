package com.chatapp.feature.creategroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.model.Contact
import com.chatapp.core.model.User
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.repository.RealConversationRepository
import com.chatapp.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGroupUiState(
    val contacts: List<Contact> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val groupName: String = "",
    val isCreating: Boolean = false,
    val createdConversationId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val contactRepo: ContactRepository,
    private val convRepo: RealConversationRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    val currentUserId: String = tokenStore.getUserId() ?: ""

    init {
        viewModelScope.launch {
            try {
                val contacts = contactRepo.getContacts().first()
                _uiState.value = _uiState.value.copy(contacts = contacts)
            } catch (_: Exception) {}
        }
    }

    fun toggleSelection(userId: String) {
        val current = _uiState.value.selectedIds
        _uiState.value = _uiState.value.copy(
            selectedIds = if (userId in current) current - userId else current + userId
        )
    }

    fun onGroupNameChange(name: String) {
        _uiState.value = _uiState.value.copy(groupName = name)
    }

    fun createGroup() {
        val state = _uiState.value
        if (state.groupName.isBlank()) {
            _uiState.value = state.copy(error = "请输入群名称")
            return
        }
        if (state.selectedIds.isEmpty()) {
            _uiState.value = state.copy(error = "请选择至少一位联系人")
            return
        }
        _uiState.value = state.copy(isCreating = true, error = null)
        viewModelScope.launch {
            try {
                val conv = convRepo.createGroup(state.groupName, state.selectedIds.toList())
                _uiState.value = _uiState.value.copy(isCreating = false, createdConversationId = conv.id)
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                }
                _uiState.value = _uiState.value.copy(isCreating = false, error = e.message ?: "创建失败")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreating = false, error = e.message ?: "网络错误")
            }
        }
    }
}
