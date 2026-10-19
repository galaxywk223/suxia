package com.galaxywk.originalmedialibrary.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.galaxywk.originalmedialibrary.android.ui.theme.LocalStatusColors

internal val ActiveStatuses = listOf("queued", "resolving", "downloading", "extracting")
internal val FailedStatuses = listOf("failed", "cancelled", "interrupted")

internal val StatusLabels = mapOf(
    "queued" to "等待中", "resolving" to "正在解析", "downloading" to "正在下载", "extracting" to "正在提取",
    "completed" to "已完成", "failed" to "失败", "cancelled" to "已取消", "interrupted" to "已中断",
)

internal fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble(); var index = 0
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return if (index == 0) "$bytes B" else "%.1f %s".format(value, units[index])
}

@Composable
internal fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape=MaterialTheme.shapes.large, color=MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp), content=content)
    }
}

@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.padding(start=4.dp, top=4.dp), style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun EmptyState(icon: ImageVector, title: String, body: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical=48.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)), contentAlignment=Alignment.Center) {
            Icon(icon, null, tint=MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(title, style=MaterialTheme.typography.titleMedium)
        body?.let { Text(it, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
internal fun statusColors(status: String): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val extra = LocalStatusColors.current
    return when (status) {
        "completed" -> extra.success to extra.successContainer
        "failed", "interrupted" -> scheme.error to scheme.errorContainer
        "cancelled" -> scheme.onSurfaceVariant to scheme.surfaceContainerHighest
        else -> scheme.onPrimaryContainer to scheme.primaryContainer
    }
}

@Composable
internal fun StatusChip(status: String) {
    val (fg, bg) = statusColors(status)
    Text(StatusLabels[status] ?: status, Modifier.background(bg, CircleShape).padding(horizontal=8.dp, vertical=2.dp),
        color=fg, style=MaterialTheme.typography.labelSmall)
}
