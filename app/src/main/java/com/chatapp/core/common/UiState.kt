package com.chatapp.core.common

/**
 * Generic UI state wrapper.
 * Every screen must expose one of these four states.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
