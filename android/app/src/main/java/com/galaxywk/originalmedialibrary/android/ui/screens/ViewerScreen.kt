package com.galaxywk.originalmedialibrary.android.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.galaxywk.originalmedialibrary.android.AssetEntity
import com.galaxywk.originalmedialibrary.android.CollectionWithAssets
import com.galaxywk.originalmedialibrary.android.MainViewModel
import java.io.File

@Composable
internal fun Viewer(vm: MainViewModel,item: CollectionWithAssets) {
    var index by remember(item.collection.id) {mutableIntStateOf(0)}
    var rename by remember {mutableStateOf(false)}
    var title by remember {mutableStateOf(item.collection.title)}
    val assets=item.assets.sortedBy {it.sequence}
    val asset=assets.getOrNull(index.coerceAtMost((assets.size-1).coerceAtLeast(0)))
    Dialog(onDismissRequest={vm.viewerId=null},properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Row(Modifier.padding(start=16.dp,end=4.dp,top=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.collection.title,maxLines=2,style=MaterialTheme.typography.titleMedium)
                        item.collection.author?.let { Text(it,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    IconButton(onClick={rename=true}){Icon(Icons.Outlined.Edit,"重命名")}
                    IconButton(onClick={vm.viewerId=null}){Icon(Icons.Outlined.Close,"关闭")}
                }
                if(item.collection.trashedAt!=null) {
                    EmptyState(Icons.Outlined.DeleteOutline,"作品在回收站内","恢复后可预览。导出副本不受影响。")
                    Button(onClick={vm.restore(item.collection.id)},Modifier.align(Alignment.CenterHorizontally)){Text("恢复")}
                } else if(asset!=null) {
                    Box(Modifier.weight(1f).fillMaxWidth().padding(12.dp).clip(MaterialTheme.shapes.large).background(Color.Black),contentAlignment=Alignment.Center) {
                        if(asset.kind=="image") AsyncImage(File(asset.path),asset.filename,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
                        else Player(asset)
                        if(assets.size>1) {
                            NavButton(Icons.Outlined.ChevronLeft,"上一项",Modifier.align(Alignment.CenterStart)){index=(index-1+assets.size)%assets.size}
                            NavButton(Icons.Outlined.ChevronRight,"下一项",Modifier.align(Alignment.CenterEnd)){index=(index+1)%assets.size}
                            Text("${index+1} / ${assets.size}",Modifier.align(Alignment.BottomCenter).padding(12.dp)
                                .background(Color.Black.copy(alpha=.55f),CircleShape).padding(horizontal=12.dp,vertical=4.dp),
                                color=Color.White,style=MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(Modifier.padding(horizontal=12.dp).fillMaxWidth(),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainer) {
                        Column(Modifier.padding(horizontal=14.dp,vertical=10.dp)) {
                            Text(asset.filename,style=MaterialTheme.typography.titleSmall)
                            Text("${asset.mimeType} · ${formatBytes(asset.size)} · ${asset.width ?: "?"} × ${asset.height ?: "?"}",
                                style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        ActionChip(Icons.AutoMirrored.Outlined.OpenInNew,"打开"){vm.open(asset)}
                        ActionChip(Icons.Outlined.FolderOpen,"定位"){vm.locate(asset)}
                        ActionChip(Icons.Outlined.IosShare,"导出"){vm.export(item.collection.id)}
                        if(asset.kind=="video") ActionChip(Icons.Outlined.Audiotrack,"提取音频"){vm.audio(item.collection.id,asset.id)}
                        ActionChip(Icons.Outlined.DeleteOutline,"移入回收站",danger=true){vm.trash(listOf(item.collection.id))}
                    }
                }
            }
        }
    }
    if(rename) AlertDialog(onDismissRequest={rename=false},title={Text("重命名作品")},text={OutlinedTextField(title,{title=it},label={Text("名称")})},
        confirmButton={TextButton(onClick={vm.rename(item.collection.id,title);rename=false}){Text("保存")}},dismissButton={TextButton(onClick={rename=false}){Text("取消")}})
}

@Composable
private fun NavButton(icon: ImageVector,label: String,modifier: Modifier,onClick:()->Unit) {
    IconButton(onClick=onClick,modifier.padding(8.dp).clip(CircleShape).background(Color.White.copy(alpha=.14f))) {
        Icon(icon,label,tint=Color.White)
    }
}

@Composable
private fun ActionChip(icon: ImageVector,label: String,danger: Boolean=false,onClick:()->Unit) {
    val color=if(danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    FilledTonalButton(onClick=onClick,colors=ButtonDefaults.filledTonalButtonColors(
        containerColor=if(danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,contentColor=color)) {
        Icon(icon,null,Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(label)
    }
}

@Composable
private fun Player(asset: AssetEntity) {
    val context=LocalContext.current
    val player=remember(asset.id) { ExoPlayer.Builder(context).build().apply {setMediaItem(MediaItem.fromUri(Uri.fromFile(File(asset.path))));prepare()} }
    DisposableEffect(player) {onDispose {player.release()}}
    AndroidView(factory={PlayerView(it).apply { this.player=player;useController=true }},Modifier.fillMaxSize())
}
