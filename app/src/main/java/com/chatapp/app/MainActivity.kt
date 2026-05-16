package com.chatapp.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.chatapp.core.navigation.AppNavHost
import com.chatapp.core.ui.theme.ChatLinkTheme
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.WebSocketManager
import com.chatapp.data.repository.RealConversationRepository
import com.chatapp.domain.repository.ConversationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore
    @Inject lateinit var wsManager: WebSocketManager
    @Inject lateinit var conversationRepo: ConversationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely determine if we have a valid token
        val hasToken = try {
            val token = tokenStore.getAccessToken()
            !token.isNullOrBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read token, clearing", e)
            try { tokenStore.clearTokens() } catch (_: Exception) {}
            false
        }

        // Wire WebSocket + connect (safe — failures don't crash)
        try {
            if (conversationRepo is RealConversationRepository) {
                val repo = conversationRepo as RealConversationRepository
                wsManager.onMessageReceived = { message ->
                    try {
                        repo.onIncomingMessage(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "onIncomingMessage error", e)
                    }
                }
            }
            if (hasToken) {
                wsManager.connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket setup failed", e)
            // Not fatal — user can still use the app without real-time
        }

        setContent {
            ChatLinkTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    startAtMain = hasToken,
                    onLoginSuccess = {
                        try { wsManager.connect() } catch (e: Exception) {
                            Log.e(TAG, "WS connect after login failed", e)
                        }
                    },
                    onLogout = {
                        try { wsManager.disconnect() } catch (_: Exception) {}
                        try { tokenStore.clearTokens() } catch (_: Exception) {}
                    },
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
