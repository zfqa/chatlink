package com.chatapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.chatapp.core.navigation.AppNavHost
import com.chatapp.core.ui.theme.ChatLinkTheme
import com.chatapp.data.remote.TokenStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val hasToken = tokenStore.getAccessToken() != null
        setContent {
            ChatLinkTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    startAtMain = hasToken,
                )
            }
        }
    }
}
