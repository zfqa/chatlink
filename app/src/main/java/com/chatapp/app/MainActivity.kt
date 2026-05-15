package com.chatapp.app

import android.os.Bundle
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
        val hasToken = tokenStore.getAccessToken() != null

        // Wire WebSocket message handler to conversation repository
        if (conversationRepo is RealConversationRepository) {
            val repo = conversationRepo as RealConversationRepository
            wsManager.onMessageReceived = { message ->
                repo.onIncomingMessage(message)
            }
        }

        // Connect WebSocket if already logged in
        if (hasToken) {
            wsManager.connect()
        }

        setContent {
            ChatLinkTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    startAtMain = hasToken,
                    onLoginSuccess = { wsManager.connect() },
                    onLogout = { wsManager.disconnect() },
                )
            }
        }
    }
}
