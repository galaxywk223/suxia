package com.galaxywk.originalmedialibrary.android.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxywk.originalmedialibrary.android.AndroidAudioEngine
import com.galaxywk.originalmedialibrary.android.BuildConfig
import com.galaxywk.originalmedialibrary.android.DouyinSession
import com.galaxywk.originalmedialibrary.android.MainViewModel
import com.galaxywk.originalmedialibrary.android.ui.theme.LocalStatusColors
import com.galaxywk.originalmedialibrary.android.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(vm: MainViewModel,revision: Int,onLogin:()->Unit) {
    val export by vm.exportTree.collectAsStateWithLifecycle()
    val chooser=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()){vm.chooseTree(it)}
    val loggedIn=remember(revision){vm.app.session.loggedIn()}
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        SectionLabel("账号")
        SettingsPanel {
            SettingsRow(Icons.Outlined.AccountCircle,"抖音登录",if(loggedIn) "已登录" else "待登录",
                tint=if(loggedIn) LocalStatusColors.current.success else MaterialTheme.colorScheme.onSurfaceVariant) {
                FilledTonalButton(onClick=onLogin){Text(if(loggedIn) "重新登录" else "配置登录")}
            }
        }
        SectionLabel("存储")
        SettingsPanel {
            SettingsRow(Icons.Outlined.Folder,"工作目录",vm.app.storage.root.toString())
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(Icons.Outlined.IosShare,"导出目录",export ?: "未选择") {
                TextButton(onClick={chooser.launch(export?.let(Uri::parse))}){Text("选择")}
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(Icons.Outlined.Refresh,"重新扫描媒体库","原文件保存在应用目录，导出后可在系统文件管理器访问") {
                TextButton(onClick=vm::scan){Text("扫描")}
            }
        }
        SectionLabel("外观")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index,mode ->
                SegmentedButton(selected=vm.theme==mode,onClick={vm.chooseTheme(mode)},
                    shape=SegmentedButtonDefaults.itemShape(index,ThemeMode.entries.size),
                    colors=SegmentedButtonDefaults.colors(activeContainerColor=MaterialTheme.colorScheme.primary,
                        activeContentColor=MaterialTheme.colorScheme.onPrimary,inactiveContainerColor=MaterialTheme.colorScheme.surfaceContainer)) {
                    Text(mode.label)
                }
            }
        }
        SectionLabel("关于")
        SettingsPanel {
            SettingsRow(Icons.Outlined.GraphicEq,"音频引擎",AndroidAudioEngine.version()?.let{"FFmpeg $it · MP3 192 kbps"} ?: "音频引擎不可用")
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(Icons.Outlined.Info,"Keepsake","Android ${BuildConfig.VERSION_NAME} · 卸载会删除内部数据，导出副本保留")
        }
    }
}

@Composable
private fun SettingsPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainer) { Column(content=content) }
}

@Composable
private fun SettingsRow(icon: ImageVector,title: String,supporting: String,
    tint: androidx.compose.ui.graphics.Color=MaterialTheme.colorScheme.onSurfaceVariant,trailing: (@Composable () -> Unit)?=null) {
    ListItem(headlineContent={Text(title)},supportingContent={Text(supporting)},
        leadingContent={Icon(icon,null,tint=tint)},trailingContent=trailing,
        colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainer,
            supportingColor=MaterialTheme.colorScheme.onSurfaceVariant))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginDialog(session: DouyinSession,onClose:()->Unit) {
    val view=session.loginView
    BackHandler {if(view.canGoBack())view.goBack() else onClose()}
    Dialog(onDismissRequest=onClose,properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)){
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) { Column(Modifier.systemBarsPadding()) {
            TopAppBar(title={Text("登录抖音")},
                navigationIcon={IconButton(onClick={if(view.canGoBack())view.goBack() else onClose()}){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"返回")}},
                actions={TextButton(onClick=onClose){Text("完成登录")}},
                colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background))
            AndroidView(factory={(view.parent as? ViewGroup)?.removeView(view);view},Modifier.weight(1f).fillMaxWidth())
        } }
    }
}
