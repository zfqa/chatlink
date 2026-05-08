package com.chatapp.feature.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Empty -> EmptyState(message = "暂无联系人")
        is UiState.Error -> ErrorView(message = state.message)
        is UiState.Content -> ContactsContent(state.data, onContactClick)
    }
}

@Composable
private fun ContactsContent(data: ContactsUiData, onContactClick: (String) -> Unit) {
    val contacts = data.filtered
    if (contacts.isEmpty()) {
        EmptyState(message = "没有匹配的联系人")
        return
    }
    val grouped = contacts.groupBy { it.pinyinInitial.ifBlank { "#" } }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
