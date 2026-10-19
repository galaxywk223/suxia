package com.galaxywk.originalmedialibrary.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxywk.originalmedialibrary.android.JobEntity
import com.galaxywk.originalmedialibrary.android.MainViewModel

@Composable
internal fun TasksScreen(vm: MainViewModel) {
    val jobs by vm.jobs.collectAsStateWithLifecycle()
    val groups=listOf(
        "进行中" to jobs.filter { it.status in ActiveStatuses },
        "需要处理" to jobs.filter { it.status in FailedStatuses },
        "已完成" to jobs.filter { it.status !in ActiveStatuses && it.status !in FailedStatuses },
    )
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        if(jobs.isEmpty()) item { EmptyState(Icons.Outlined.Inbox,"暂无任务","在下载页粘贴链接开始下载") }
        groups.filter { it.second.isNotEmpty() }.forEach { (label,list) ->
            item(key=label) { SectionLabel("$label · ${list.size}") }
            items(list,key={it.id}){job -> JobCard(vm,job)}
        }
    }
}

@Composable
private fun JobCard(vm: MainViewModel,job: JobEntity) {
    val (fg,bg)=statusColors(job.status)
    val active=job.status in ActiveStatuses
    Surface(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).background(bg,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
                when {
                    active -> CircularProgressIndicator(Modifier.size(20.dp),color=fg,strokeWidth=2.dp)
                    job.status=="completed" -> Icon(Icons.Outlined.CheckCircle,null,tint=fg)
                    job.status=="cancelled" -> Icon(Icons.Outlined.Cancel,null,tint=fg)
                    else -> Icon(Icons.Outlined.ErrorOutline,null,tint=fg)
                }
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(job.title,Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.titleSmall)
                    StatusChip(job.status)
                }
                if(active) {
                    LinearProgressIndicator(progress={job.progress/100f},modifier=Modifier.fillMaxWidth().height(5.dp),
                        color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap=StrokeCap.Round,gapSize=0.dp,drawStopIndicator={})
                    Text("${job.progress}% · ${formatBytes(job.downloadedBytes)} / ${if(job.totalBytes>0) formatBytes(job.totalBytes) else "未知"}",
                        style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                job.error?.let {Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
                if(active) TextButton(onClick={vm.cancel(job)},contentPadding=PaddingValues(0.dp)){Text("取消任务")}
                if(job.status in FailedStatuses) TextButton(onClick={vm.retry(job)},contentPadding=PaddingValues(0.dp)){Text("重试任务")}
            }
        }
    }
}
