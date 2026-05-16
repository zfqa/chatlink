package com.chatapp.feature.friendrequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.UiState
import com.chatapp.core.model.FriendRequest
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.EmptyState
import com.chatapp.core.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    onAuthError: () -> Unit = {},
    viewModel: FriendRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    LaunchedEffect(authError) {
        if (authError) onAuthError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友请求") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        when (val state = uiState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Empty -> EmptyState(message = "暂无好友请求", modifier = Modifier.padding(padding))
            is UiState.Error -> EmptyState(message = state.message, modifier = Modifier.padding(padding))
            is UiState.Content -> FriendRequestsList(
                requests = state.data.requests,
                onAccept = viewModel::accept,
                onReject = viewModel::reject,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun FriendRequestsList(
    requests: List<FriendRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(requests, key = { it.id }) { request ->
            FriendRequestItem(request, onAccept, onReject)
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
        }
    }
}

@Composable
private fun FriendRequestItem(
    request: FriendRequest,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = request.fromUser.nickname)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = request.fromUser.nickname, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = request.fromUser.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Row {
            TextButton(onClick = { onReject(request.id) }) {
                Text("拒绝", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.width(4.dp))
            Button(onClick = { onAccept(request.id) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("接受")
            }
        }
    }
}
