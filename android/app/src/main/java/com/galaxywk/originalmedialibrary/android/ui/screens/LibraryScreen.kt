package com.galaxywk.originalmedialibrary.android.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.galaxywk.originalmedialibrary.android.CollectionWithAssets
import com.galaxywk.originalmedialibrary.android.LibraryFilter
import com.galaxywk.originalmedialibrary.android.LibraryQuery
import com.galaxywk.originalmedialibrary.android.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(vm: MainViewModel, all: List<CollectionWithAssets>) {
    var search by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf("all") }
    var sort by rememberSaveable { mutableStateOf("newest") }
    var trash by rememberSaveable { mutableStateOf(false) }
    var grid by rememberSaveable { mutableStateOf(true) }
    var sortMenu by remember { mutableStateOf(false) }
    val filtered=LibraryFilter.apply(all,LibraryQuery(search,kind,sort,trash))
    val importer=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { vm.import(it) }
    val sorts=listOf("newest" to "最新","oldest" to "最早","name" to "名称","size" to "文件数量")
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(search,{search=it},Modifier.weight(1f).height(52.dp),singleLine=true,
                placeholder={Text("搜索媒体")}, leadingIcon={Icon(Icons.Outlined.Search,null)}, shape=CircleShape,
                colors=OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor=MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor=MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedBorderColor=MaterialTheme.colorScheme.outlineVariant,
                ))
            IconButton(onClick=vm::scan){Icon(Icons.Outlined.Refresh,"重新扫描")}
            IconButton(onClick={importer.launch(arrayOf("image/*","video/*","audio/*"))}){Icon(Icons.Outlined.AddPhotoAlternate,"导入媒体")}
        }
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("all" to "全部","image" to "图片","video" to "视频","audio" to "音频").forEach { (value,label) ->
                FilterChip(kind==value,{kind=value},label={Text(label)},shape=CircleShape,colors=chipColors())
            }
            FilterChip(trash,{trash=!trash; vm.selected=emptySet()},label={Text("回收站")},shape=CircleShape,colors=chipColors(),
                leadingIcon={Icon(Icons.Outlined.DeleteOutline,null,Modifier.size(16.dp))})
        }
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text("${filtered.size} 个作品",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
            Box {
                TextButton(onClick={sortMenu=true}) {
                    Icon(Icons.Outlined.SwapVert,null,Modifier.size(18.dp)); Spacer(Modifier.width(4.dp))
                    Text(sorts.first { it.first==sort }.second)
                }
                DropdownMenu(sortMenu,{sortMenu=false}) {
                    sorts.forEach { (value,label) -> DropdownMenuItem(text={Text(label)},onClick={sort=value;sortMenu=false},
                        trailingIcon={ if(sort==value) Icon(Icons.Outlined.Check,null) }) }
                }
            }
            IconButton(onClick={grid=!grid}) {
                Icon(if(grid) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.GridView, if(grid) "列表视图" else "网格视图")
            }
        }
        if(vm.selected.isNotEmpty()&&!trash) {
            Surface(shape=CircleShape,color=MaterialTheme.colorScheme.surfaceContainerHighest) {
                Row(Modifier.padding(start=16.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text("${vm.selected.size} 项已选择",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
                    TextButton(onClick={vm.selected=emptySet()}){Text("取消选择")}
                    TextButton(onClick={vm.trash(vm.selected.toList())},colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)){Text("移入回收站")}
                }
            }
        }
        if(filtered.isEmpty()) {
            EmptyState(Icons.Outlined.PhotoLibrary, if(trash) "回收站为空" else if(search.isNotBlank()) "没有匹配的媒体" else "媒体库为空",
                if(trash) null else "下载作品或导入本地文件")
        } else if(grid) {
            LazyVerticalGrid(GridCells.Adaptive(150.dp),Modifier.weight(1f),contentPadding=PaddingValues(bottom=16.dp),
                horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                items(filtered,key={it.collection.id}) { item -> PosterCard(vm,item,trash) }
            }
        } else {
            LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                items(filtered,key={it.collection.id}){item -> MediaRow(vm,item,trash)}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor=MaterialTheme.colorScheme.primary,
    selectedLabelColor=MaterialTheme.colorScheme.onPrimary,
    selectedLeadingIconColor=MaterialTheme.colorScheme.onPrimary,
)

private fun CollectionWithAssets.cover() = assets.sortedBy { it.sequence }.firstOrNull { it.kind != "audio" }

private fun CollectionWithAssets.subtitle() = "${assets.size} 个文件 · ${collection.author ?: "本地文件"}"

@Composable
private fun PosterCard(vm: MainViewModel,item: CollectionWithAssets,trash: Boolean) {
    val selected=item.collection.id in vm.selected
    val shape=RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth().aspectRatio(3f/4f).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .then(if(selected) Modifier.border(3.dp,MaterialTheme.colorScheme.primary,shape) else Modifier)
        .clickable{vm.viewerId=item.collection.id}) {
        val cover=item.cover()
        if(!trash && cover!=null) AsyncImage(cover.thumbnail ?: cover.path.let(::File),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        else Icon(if(item.collection.mediaType=="audio") Icons.Outlined.Audiotrack else Icons.Outlined.Image,null,
            Modifier.align(Alignment.Center).size(32.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
        if(item.collection.mediaType=="video"||item.collection.mediaType=="mixed") MediaBadge(Icons.Outlined.PlayArrow,Modifier.align(Alignment.TopEnd))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent,Color.Black.copy(alpha=.82f))))
            .padding(start=12.dp,end=12.dp,top=28.dp,bottom=10.dp)) {
            Text(item.collection.title,color=Color.White,maxLines=2,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.titleSmall)
            Text(item.subtitle(),color=Color.White.copy(alpha=.7f),maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.bodySmall)
            if(trash) TextButton(onClick={vm.restore(item.collection.id)},contentPadding=PaddingValues(0.dp)){Text("恢复",color=MaterialTheme.colorScheme.primary)}
        }
        if(!trash) Box(Modifier.align(Alignment.TopStart).padding(8.dp).size(26.dp).clip(RoundedCornerShape(8.dp))
            .background(if(selected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha=.35f))
            .border(1.5.dp,if(selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=.8f),RoundedCornerShape(8.dp))
            .clickable{vm.toggleSelected(item.collection.id)},contentAlignment=Alignment.Center) {
            if(selected) Icon(Icons.Outlined.Check,"取消选择",Modifier.size(16.dp),tint=MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun MediaBadge(icon: ImageVector,modifier: Modifier) {
    Box(modifier.padding(8.dp).size(28.dp).clip(CircleShape).background(Color.Black.copy(alpha=.45f)),contentAlignment=Alignment.Center) {
        Icon(icon,null,Modifier.size(18.dp),tint=Color.White)
    }
}

@Composable
private fun MediaRow(vm: MainViewModel,item: CollectionWithAssets,trash: Boolean) {
    val selected=item.collection.id in vm.selected
    Surface(Modifier.fillMaxWidth().clickable{vm.viewerId=item.collection.id},shape=MaterialTheme.shapes.medium,
        color=if(selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            val cover=item.cover()
            Box(Modifier.size(width=48.dp,height=64.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                if(!trash && cover!=null) AsyncImage(cover.thumbnail ?: cover.path.let(::File),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            }
            Column(Modifier.weight(1f)) {
                Text(item.collection.title,maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.titleSmall)
                Text(item.subtitle(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if(trash) TextButton(onClick={vm.restore(item.collection.id)}){Text("恢复")}
            else Checkbox(selected,{vm.toggleSelected(item.collection.id)})
        }
    }
}
