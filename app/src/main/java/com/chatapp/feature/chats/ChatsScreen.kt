package com.chatapp.feature.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.DateTimeUtils
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Conversation
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.EmptyState
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView

@Composable
fun ChatsScreen(
    onConversationClick: (String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Empty -> EmptyState(message = "暂无会话")
        is UiState.Error -> ErrorView(message = state.message, onRetry = null)
        is UiState.Content -> ChatsContent(data = state.data, onItemClick = onConversationClick)
    }
}

@Composable
private fun ChatsContent(data: ChatsUiData, onItemClick: (String) -> Unit) {
    val conversations = data.filtered
    if (conversations.isEmpty()) {
        EmptyState(message = "没有匹配的会话")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(conversations, key = { it.id }) { conv ->
            ConversationItem(conv, onClick = { onItemClick(conv.id) })
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
        }
    }
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = conversation.peer.nickname)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = conversation.peer.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.weight(1f))
                Text(text = DateTimeUtils.formatTimestamp(conversation.lastMessageTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = conversation.lastMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (conversation.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text(text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(), color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}
