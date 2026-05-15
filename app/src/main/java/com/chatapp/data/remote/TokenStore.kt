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

    fun isLoggedIn(): Boolean = getAccessToken() != null

    /**
     * Decode userId from JWT payload (base64). Caches the result.
     */
    fun getUserId(): String? {
        cachedUserId?.let { return it }
        val token = getAccessToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val json = org.json.JSONObject(String(payload))
            val id = json.optString("userId", null) ?: json.optString("sub", null)
            cachedUserId = id
            id
        } catch (_: Exception) {
            null
        }
    }

    private var cachedUserId: String? = null

    fun clearTokens() {
        prefs.edit().clear().apply()
        cachedUserId = null
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
