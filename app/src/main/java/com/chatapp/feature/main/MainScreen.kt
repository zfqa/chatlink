package com.chatapp.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chatapp.feature.chats.ChatsScreen
import com.chatapp.feature.contacts.ContactsScreen
import com.chatapp.feature.discover.DiscoverScreen
import com.chatapp.feature.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onConversationClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val items = BottomNavItem.entries
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Determine current tab title
    val currentTitle = items.find { it.route == currentRoute }?.label ?: "ChatLink"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.CHATS.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomNavItem.CHATS.route) {
                ChatsScreen(onConversationClick = onConversationClick)
            }
            composable(BottomNavItem.CONTACTS.route) {
                ContactsScreen()
            }
            composable(BottomNavItem.DISCOVER.route) {
                DiscoverScreen()
            }
            composable(BottomNavItem.PROFILE.route) {
            ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
