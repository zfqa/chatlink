package com.chatapp.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatapp.core.common.UiState
import com.chatapp.core.ui.components.Avatar
import com.chatapp.core.ui.components.ErrorView
import com.chatapp.core.ui.components.LoadingView
import com.chatapp.core.ui.components.LoadingView

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onAuthError: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    LaunchedEffect(authError) {
        if (authError) onAuthError()
    }

    when (val state = uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Empty -> LoadingView()
        is UiState.Error -> ErrorView(message = state.message)
        is UiState.Content -> ProfileContent(state.data, onLogout = { viewModel.logout(onLogout) })
    }
}

@Composable
private fun ProfileContent(data: ProfileUiData, onLogout: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name = data.user.nickname, size = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = data.user.nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(text = "账号: ${data.user.email.ifBlank { data.user.id }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        if (data.user.signature.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(text = data.user.signature, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        items(data.menuItems) { menuItem ->
            MenuItemRow(menuItem)
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
        }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.title.first().toString(), fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text = item.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (item.subtitle.isNotBlank()) {
            Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        Spacer(Modifier.width(4.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}
