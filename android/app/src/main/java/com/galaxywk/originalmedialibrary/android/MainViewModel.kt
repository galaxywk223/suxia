package com.galaxywk.originalmedialibrary.android

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galaxywk.originalmedialibrary.android.ui.theme.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(val app: MediaDownloaderApplication) : ViewModel() {
    private val uiPrefs = app.getSharedPreferences("ui", Context.MODE_PRIVATE)
    val library = app.library.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val jobs = app.downloads.jobs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val exportTree = app.settings.exportTree.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    var message by mutableStateOf<String?>(null); private set
    var busy by mutableStateOf(false); private set
    var sources by mutableStateOf<List<ParsedSource>>(emptyList()); private set
    var chosen by mutableStateOf<Set<String>>(emptySet())
    var selected by mutableStateOf<Set<String>>(emptySet())
    var viewerId by mutableStateOf<String?>(null)
    var theme by mutableStateOf(
        ThemeMode.entries.firstOrNull { it.name == uiPrefs.getString("theme", null) } ?: ThemeMode.Dark,
    ); private set
    fun run(action: suspend () -> String?) {
        viewModelScope.launch {
            try { action()?.let { message = it } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { message = error.message ?: "操作失败" }
        }
    }
    fun parse(text: String) {
        run {
            busy = true
            try {
                sources = app.downloads.parse(text)
                chosen = sources.filter { !it.duplicate }.map { it.url }.toSet()
                "已识别 ${sources.size} 个作品"
            } finally { busy = false }
        }
    }
    fun queue(force: Boolean) = run { "已创建 ${app.downloads.queue(sources.filter { it.url in chosen }, force)} 个任务" }
    fun toggleSelected(id: String) { selected = if(id in selected) selected-id else selected+id }
    fun scan() = run { "已扫描，新增 ${app.library.scan()} 个文件" }
    fun import(uris: List<Uri>) = run { "已导入 ${app.library.importUris(uris)} 个文件" }
    fun export(id: String) = run {
        require(exportTree.value != null) { "请在设置中选择导出目录" }
        app.library.export(id); "已导出"
    }
    fun trash(ids: List<String>) = run { app.library.trash(ids); selected=emptySet(); viewerId=null; "已移入回收站（导出副本保留）" }
    fun restore(id: String) = run { app.library.restore(id); viewerId=null; "已恢复" }
    fun rename(id: String, title: String) = run { require(title.isNotBlank()) { "名称不能为空" }; app.library.rename(id,title); "已重命名" }
    fun audio(id: String, assetId: String) = run { app.audio.queue(id,assetId); "提取任务已排队" }
    fun cancel(job: JobEntity) = run { app.downloads.cancel(job); null }
    fun retry(job: JobEntity) = run { app.downloads.retry(job); "已排队重试" }
    fun open(asset: AssetEntity) = run { app.storage.open(asset); null }
    fun locate(asset: AssetEntity) = run { app.storage.locate(asset); null }
    fun chooseTree(uri: Uri?) = run { if(uri != null) app.settings.select(uri); "导出目录已设置" }
    fun chooseTheme(mode: ThemeMode) { theme = mode; uiPrefs.edit().putString("theme", mode.name).apply() }
    fun dismiss() { message = null }
}
