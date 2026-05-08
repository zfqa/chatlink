package com.chatapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.chatapp.core.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("chatlink_auth", Context.MODE_PRIVATE)

    fun saveSession(user: User, email: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_NICKNAME, user.nickname)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getSavedUser(): User? {
        if (!isLoggedIn()) return null
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val nickname = prefs.getString(KEY_NICKNAME, "") ?: ""
        return User(id = id, nickname = nickname, avatarUrl = "", signature = "")
    }

    companion object {
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
    }
}
