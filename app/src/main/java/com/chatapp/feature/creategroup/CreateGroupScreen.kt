package com.chatapp.feature.creategroup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.ui.components.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdConversationId) {
        uiState.createdConversationId?.let { onGroupCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建群聊") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.createGroup() },
                        enabled = !uiState.isCreating && uiState.selectedIds.isNotEmpty() && uiState.groupName.isNotBlank(),
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("创建", color = if (uiState.selectedIds.isNotEmpty() && uiState.groupName.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                        }
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
            // Group name input
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = viewModel::onGroupNameChange,
                label = { Text("群名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Selected count
            if (uiState.selectedIds.isNotEmpty()) {
                Text(
                    text = "已选择 ${uiState.selectedIds.size} 人",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // Error
            uiState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
            }

            HorizontalDivider()

            // Contact list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.contacts, key = { it.user.id }) { contact ->
                    val isSelected = contact.user.id in uiState.selectedIds
                    ContactCheckItem(
                        name = contact.user.nickname,
                        isSelected = isSelected,
                        onClick = { viewModel.toggleSelection(contact.user.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactCheckItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        Spacer(Modifier.width(8.dp))
        Avatar(name = name)
        Spacer(Modifier.width(12.dp))
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}
