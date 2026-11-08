package com.xrotune.music.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.domain.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val downloads: DownloadRepository,
) : ViewModel() {

    val items: StateFlow<List<DownloadEntity>> =
        downloads.observeDownloads().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancel(songId: String) = viewModelScope.launch { downloads.cancel(songId) }
    fun remove(songId: String) = viewModelScope.launch { downloads.remove(songId) }
    fun clearAll() = viewModelScope.launch { downloads.clearAll() }
}
