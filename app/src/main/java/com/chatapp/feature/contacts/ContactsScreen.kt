package com.chatapp.feature.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Contact
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.EmptyState
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView
@Composable

fun ContactsScreen(
    onContactClick: (String) -> Unit,
    onAddFriend: () -> Unit,
    onFriendRequests: () -> Unit,
    onAuthError: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingRequestCount.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    LaunchedEffect(authError) {
        if (authError) onAuthError()
    }
    when (val state = uiState) {
        is UiState.Loading -> {
            ContactsContent(
                data = ContactsUiData(emptyList()),
                onContactClick = onContactClick,
                onAddFriend = onAddFriend,
                onFriendRequests = onFriendRequests,
                pendingCount = pendingCount,
            )
        }
        is UiState.Error -> ErrorView(message = state.message)
        else -> ContactsContent(
            data = if (state is UiState.Content) state.data else ContactsUiData(emptyList()),
            onContactClick = onContactClick,
            onAddFriend = onAddFriend,
            onFriendRequests = onFriendRequests,
            pendingCount = pendingCount,
        )
    }
}


@Composable

private fun ContactsContent(
    data: ContactsUiData,
    onContactClick: (String) -> Unit,
    onAddFriend: () -> Unit,
    onFriendRequests: () -> Unit,
    pendingCount: Int,
) {
    val contacts = data.filtered
    val grouped = contacts.groupBy { it.pinyinInitial.ifBlank { "#" } }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Add Friend entry
        item(key = "add_friend") {
            ActionItem(title = "添加好友", subtitle = "搜索并添加新朋友", onClick = onAddFriend)
        }
        // Friend Requests entry
        item(key = "friend_requests") {
            ActionItem(
                title = "好友请求",
                subtitle = if (pendingCount > 0) "${pendingCount} 条待处理" else "",
                onClick = onFriendRequests,
            )
        }
        item(key = "divider") { HorizontalDivider(modifier = Modifier.padding(start = 16.dp)) }
        if (contacts.isEmpty()) {
            item { EmptyState(message = "暂无联系人") }
        }
        grouped.forEach { (initial, group) ->
            item(key = "header_" + initial) {

                Text(text = initial, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

            }

            items(group, key = { it.user.id }) { contact ->
                ContactItem(contact, onClick = { onContactClick(contact.user.id) })
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
            }

        }

    }

}



@Composable

private fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,

    ) {

        Avatar(name = contact.user.nickname)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(text = contact.user.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

            if (contact.user.signature.isNotBlank()) {

                Text(text = contact.user.signature, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            }

        }

        if (contact.isOnline) {

            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {

                Text(text = "在线", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))

            }

        }

    }

}

@Composable
private fun ActionItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (title == "好友请求" && subtitle.contains("待处理")) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}
