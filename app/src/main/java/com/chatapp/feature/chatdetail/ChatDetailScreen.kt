package com.chatapp.feature.chatdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.UiState
import com.chatapp.core.model.Message
import com.chatapp.core.ui.components.EmptyState
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView
import com.chatapp.core.ui.theme.ChatBubbleOther
import com.chatapp.core.ui.theme.ChatBubbleSelf
import com.chatapp.data.repository.FakeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    val peerName = when (val s = uiState) {
        is UiState.Content -> s.data.peerName
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerName.ifBlank { "聊天" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is UiState.Loading -> LoadingView()
                    is UiState.Empty -> EmptyState(message = "发送一条消息开始聊天吧")
                    is UiState.Error -> ErrorView(message = state.message)
                    is UiState.Content -> MessageList(state.data.messages)
                }
            }
            ChatInputBar(text = inputText, onTextChange = viewModel::onInputChange, onSend = viewModel::sendMessage)
        }
    }
}

@Composable
private fun MessageList(messages: List<Message>) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.lastIndex) }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(messages, key = { it.id }) { msg ->
            MessageBubble(message = msg)
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isSelf = message.senderId == FakeData.me.id
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier.widthIn(max = 280.dp).background(color = if (isSelf) ChatBubbleSelf else ChatBubbleOther, shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        ) { Text(text = message.content, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSend, enabled = text.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送", tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}