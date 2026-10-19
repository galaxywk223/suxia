package com.galaxywk.originalmedialibrary.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxywk.originalmedialibrary.android.ui.screens.*
import com.galaxywk.originalmedialibrary.android.ui.theme.KeepsakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MediaDownloaderApp(application as MediaDownloaderApplication) }
    }
}

private enum class Page(val label: String, val subtitle: String, val icon: ImageVector) {
    Download("下载", "粘贴链接，原样收藏喜欢的作品", Icons.Outlined.Download),
    Library("媒体库", "本地收藏的图片、视频与音频", Icons.Outlined.PhotoLibrary),
    Tasks("任务", "下载队列与最近任务", Icons.AutoMirrored.Outlined.FormatListBulleted),
    Settings("设置", "登录、存储、外观与关于", Icons.Outlined.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDownloaderApp(app: MediaDownloaderApplication) {
    val vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory=object: ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T: ViewModel> create(modelClass: Class<T>): T = MainViewModel(app) as T
    })
    var page by rememberSaveable { mutableStateOf(Page.Download) }
    var login by rememberSaveable { mutableStateOf(false) }
    var sessionRevision by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val notification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { if(Build.VERSION.SDK_INT>=33) notification.launch(Manifest.permission.POST_NOTIFICATIONS) }
    LaunchedEffect(vm.message) { vm.message?.let { snackbar.showSnackbar(it); vm.dismiss() } }
    val items by vm.library.collectAsStateWithLifecycle()
    KeepsakeTheme(vm.theme) {
        Scaffold(
            containerColor=MaterialTheme.colorScheme.background,
            snackbarHost={ SnackbarHost(snackbar) { Snackbar(it, shape=MaterialTheme.shapes.medium,
                containerColor=MaterialTheme.colorScheme.surfaceContainerHighest, contentColor=MaterialTheme.colorScheme.onSurface) } },
            topBar={
                TopAppBar(
                    title={ Column {
                        Text(page.label, style=MaterialTheme.typography.headlineSmall)
                        Text(page.subtitle, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                    } },
                    colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background),
                    expandedHeight=84.dp,
                )
            },
            bottomBar={
                NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation=0.dp) {
                    Page.entries.forEach { entry ->
                        NavigationBarItem(selected=page==entry, onClick={page=entry},
                            icon={Icon(entry.icon, null)}, label={Text(entry.label)},
                            colors=NavigationBarItemDefaults.colors(
                                selectedIconColor=MaterialTheme.colorScheme.onPrimary,
                                indicatorColor=MaterialTheme.colorScheme.primary,
                                selectedTextColor=MaterialTheme.colorScheme.onSurface,
                            ))
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when(page) {
                    Page.Download -> DownloadScreen(vm)
                    Page.Library -> LibraryScreen(vm, items)
                    Page.Tasks -> TasksScreen(vm)
                    Page.Settings -> SettingsScreen(vm, sessionRevision) { app.session.openLogin(); login=true }
                }
            }
        }
        if(login) LoginDialog(app.session) { app.session.flush(); login=false; sessionRevision++ }
        vm.viewerId?.let { id -> items.find { it.collection.id==id }?.let { Viewer(vm,it) } }
    }
}
