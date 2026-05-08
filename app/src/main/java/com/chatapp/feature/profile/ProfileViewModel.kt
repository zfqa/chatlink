package com.chatapp.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.core.common.UiState
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProfileUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProfileUiData>> = _uiState.asStateFlow()

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
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }
}
