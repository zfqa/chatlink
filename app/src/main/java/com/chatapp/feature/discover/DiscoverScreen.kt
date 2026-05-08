package com.chatapp.feature.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DiscoverScreen() {
    val items = listOf(
        DiscoverItem(Icons.Default.People, "朋友圈", "查看好友动态"),
        DiscoverItem(Icons.Default.QrCodeScanner, "扫一扫", "扫描二维码"),
        DiscoverItem(Icons.Default.ShoppingCart, "购物", "精选好物推荐"),
        DiscoverItem(Icons.Default.Games, "游戏", "热门小游戏"),
        DiscoverItem(Icons.Default.Search, "搜一搜", "搜索内容和服务"),
        DiscoverItem(Icons.Default.LocationOn, "附近", "发现附近的人和事"),
        DiscoverItem(Icons.Default.Article, "看一看", "热点资讯"),
    )

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(items) { item ->
            DiscoverRow(item)
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
        }
    }
}

@Composable
private fun DiscoverRow(item: DiscoverItem) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = item.icon, contentDescription = item.title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}

private data class DiscoverItem(val icon: ImageVector, val title: String, val subtitle: String)