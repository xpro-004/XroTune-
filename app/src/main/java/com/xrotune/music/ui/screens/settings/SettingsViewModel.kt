package com.xrotune.music.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.data.local.prefs.PrefsManager
import com.xrotune.music.domain.repository.DownloadRepository
import com.xrotune.music.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PrefsManager,
    private val downloads: DownloadRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    val wifiOnly: StateFlow<Boolean> =
        prefs.wifiOnlyDownload.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highQuality: StateFlow<Boolean> =
        prefs.highQuality.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoplay: StateFlow<Boolean> =
        prefs.autoplay.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    fun setWifiOnly(enabled: Boolean) = viewModelScope.launch { prefs.setWifiOnlyDownload(enabled) }
    fun setHighQuality(enabled: Boolean) = viewModelScope.launch { prefs.setHighQuality(enabled) }
    fun setAutoplay(enabled: Boolean) = viewModelScope.launch { prefs.setAutoplay(enabled) }

    fun setLanguage(lang: String) = viewModelScope.launch { prefs.setLanguage(lang) }

    fun clearSearchHistory() = viewModelScope.launch {
        library.clearSearchHistory()
        _messages.emit("Search history cleared")
    }

    fun clearDownloads() = viewModelScope.launch {
        downloads.clearAll()
        _messages.emit("Downloads cleared")
    }
}
