package com.galaxywk.originalmedialibrary.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.galaxywk.originalmedialibrary.android.MainViewModel

@Composable
internal fun DownloadScreen(vm: MainViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    var force by rememberSaveable { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard {
                Column(verticalArrangement=Arrangement.spacedBy(2.dp)) {
                    Text("粘贴分享内容", style=MaterialTheme.typography.titleMedium)
                    Text("支持分享文案、短链接和多个抖音作品链接", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(text, {text=it}, Modifier.fillMaxWidth().heightIn(min=140.dp),
                    placeholder={Text("抖音分享文案、短链接或作品链接")}, shape=MaterialTheme.shapes.medium,
                    colors=OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor=MaterialTheme.colorScheme.background,
                        focusedContainerColor=MaterialTheme.colorScheme.background,
                        unfocusedBorderColor=MaterialTheme.colorScheme.outlineVariant,
                    ))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick={ clipboard.getText()?.text?.let { text=it } }, Modifier.weight(1f)) {
                        Icon(Icons.Outlined.ContentPaste, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("粘贴")
                    }
                    Button(onClick={vm.parse(text)}, enabled=text.isNotBlank()&&!vm.busy, modifier=Modifier.weight(1f)) {
                        Icon(Icons.Outlined.Link, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(if(vm.busy) "解析中…" else "解析链接")
                    }
                }
            }
        }
        if(vm.sources.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(top=8.dp, start=4.dp, end=4.dp), verticalAlignment=Alignment.CenterVertically) {
                    Text("解析结果", Modifier.weight(1f), style=MaterialTheme.typography.titleMedium)
                    Text("${vm.chosen.size} / ${vm.sources.size} 已选择", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(vm.sources, key={it.url}) { source ->
                val checked = source.url in vm.chosen
                val toggle = { vm.chosen = if(checked) vm.chosen-source.url else vm.chosen+source.url }
                Surface(Modifier.fillMaxWidth().clickable(onClick=toggle), shape=MaterialTheme.shapes.medium,
                    color=if(checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.padding(horizontal=8.dp, vertical=6.dp), verticalAlignment=Alignment.CenterVertically) {
                        Checkbox(checked, {toggle()})
                        Column(Modifier.weight(1f)) {
                            Text(source.awemeId?.let { "抖音作品 $it" } ?: "抖音分享链接", style=MaterialTheme.typography.titleSmall)
                            Text(source.url, maxLines=1, overflow=TextOverflow.Ellipsis, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if(source.duplicate) Text("已在媒体库", Modifier.padding(horizontal=8.dp),
                            color=MaterialTheme.colorScheme.secondary, style=MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Surface(Modifier.fillMaxWidth(), shape=MaterialTheme.shapes.medium, color=MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.padding(horizontal=16.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("强制重新下载", style=MaterialTheme.typography.titleSmall)
                            Text("已在媒体库中的作品也重新下载", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(force, {force=it})
                    }
                }
            }
            item {
                Button(onClick={vm.queue(force)}, enabled=vm.chosen.isNotEmpty(), modifier=Modifier.fillMaxWidth().height(48.dp), shape=CircleShape) {
                    Icon(Icons.Outlined.Download, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("下载选中作品（${vm.chosen.size}）")
                }
            }
        }
    }
}
