package com.chatapp.data.remote

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chatlink_token", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
