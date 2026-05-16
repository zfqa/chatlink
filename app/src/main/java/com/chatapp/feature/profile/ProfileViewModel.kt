package com.chatapp.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.TokenStore
import com.chatapp.domain.repository.AuthRepository
import com.chatapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProfileUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProfileUiData>> = _uiState.asStateFlow()

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                val menus = listOf(
                    ProfileMenuItem("payment", "支付", "钱包、收付款"),
                    ProfileMenuItem("favorite", "收藏", ""),
                    ProfileMenuItem("album", "相册", ""),
                    ProfileMenuItem("card", "包包", ""),
                    ProfileMenuItem("emoji", "表情", ""),
                    ProfileMenuItem("settings", "设置", "账号与安全、通知、隐私"),
                )
                _uiState.value = UiState.Content(ProfileUiData(user, menus))
            } catch (e: ApiException) {
                if (e.httpCode == 401) {
                    tokenStore.clearTokens()
                    _authError.value = true
                }
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepo.logout()
            } catch (_: Exception) {}
            onDone()
        }
    }
}
