package com.chatapp.feature.contactdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.UiState
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAuthError: () -> Unit = {},
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    LaunchedEffect(authError) {
        if (authError) onAuthError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Info") },
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
            is UiState.Error -> ErrorView(message = state.message, modifier = Modifier.padding(padding))
            is UiState.Content -> ContactDetailContent(
                data = state.data,
                modifier = Modifier.padding(padding),
                onSendMessage = { onSendMessage(state.data.conversationId) },
            )
            is UiState.Empty -> ErrorView(message = "Contact not found", modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun ContactDetailContent(
    data: ContactDetailUiData,
    modifier: Modifier = Modifier,
    onSendMessage: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        Avatar(name = data.contact.user.nickname, size = 80.dp)
        Spacer(Modifier.height(16.dp))

        // Nickname
        Text(
            text = data.contact.user.nickname,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))

        // Account
        Text(
            text = "Account: ${data.contact.user.id}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(2.dp))

        // Online status
        Text(
            text = if (data.contact.isOnline) "Online" else "Offline",
            style = MaterialTheme.typography.bodySmall,
            color = if (data.contact.isOnline) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(24.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Remark", data.contact.remark.ifBlank { "-" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("Signature", data.contact.user.signature.ifBlank { "-" })
            }
        }

        Spacer(Modifier.height(32.dp))

        // Send message button
        Button(
            onClick = onSendMessage,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text("Send Message")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
