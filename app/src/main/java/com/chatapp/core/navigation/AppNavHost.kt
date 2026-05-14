package com.chatapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chatapp.feature.friendrequest.FriendRequestsScreen
import com.chatapp.feature.searchuser.SearchUserScreen
import com.chatapp.feature.contactdetail.ContactDetailScreen
import com.chatapp.feature.auth.LoginScreen
import com.chatapp.feature.auth.RegisterScreen
import com.chatapp.feature.main.MainScreen
import com.chatapp.feature.chatdetail.ChatDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startAtMain: Boolean = false,
) {
    NavHost(
        navController = navController,
        startDestination = if (startAtMain) Routes.MAIN else Routes.LOGIN,
        modifier = modifier,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(Routes.chatDetail(conversationId))
                },
                onContactClick = { userId ->
                    navController.navigate(Routes.contactDetail(userId))
                },
                onAddFriend = {
                    navController.navigate(Routes.SEARCH_USER)
                },
                onFriendRequests = {
                    navController.navigate(Routes.FRIEND_REQUESTS)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.SEARCH_USER) {
            SearchUserScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.FRIEND_REQUESTS) {
            FriendRequestsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.CONTACT_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            ContactDetailScreen(
                onBack = { navController.popBackStack() },
                onSendMessage = { conversationId ->
                    navController.navigate(Routes.chatDetail(conversationId))
                },
            )
        }

        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            ChatDetailScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
