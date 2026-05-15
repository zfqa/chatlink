package com.chatapp.feature.searchuser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.chatapp.core.model.User
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.EmptyState
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserScreen(
    onBack: () -> Unit,
    onAuthError: () -> Unit = {},
    viewModel: SearchUserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    LaunchedEffect(authError) {
        if (authError) onAuthError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Friend") },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SearchBar(
                query = if (uiState is UiState.Content) (uiState as UiState.Content).data.query else "",
                onQueryChange = viewModel::onQueryChange,
            )
            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(message = state.message)
                is UiState.Empty -> EmptyState(message = "No results")
                is UiState.Content -> {
                    if (state.data.results.isEmpty()) {
                        EmptyState(message = "No users found")
                    } else {
                        SearchResultsList(state.data, viewModel::sendRequest)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search by nickname or account...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
    )
}

@Composable
private fun SearchResultsList(
    data: SearchUserUiData,
    onAddFriend: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(data.results, key = { it.id }) { user ->
            SearchResultItem(
                user = user,
                isFriend = user.id in data.friendIds,
                isPending = user.id in data.pendingIds,
                onAddFriend = { onAddFriend(user.id) },
            )
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
        }
    }
}

@Composable
private fun SearchResultItem(
    user: User,
    isFriend: Boolean,
    isPending: Boolean,
    onAddFriend: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = user.nickname)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.nickname, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = user.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        when {
            isFriend -> {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Text(
                        text = "Friend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            isPending -> {
                Text(
                    text = "Requested",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
            else -> {
                Button(onClick = onAddFriend, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
