package com.xrotune.music.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.data.local.db.entity.toDomain
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.DownloadRepository
import com.xrotune.music.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val label: String) { FAVOURITES("Favourites"), PLAYLISTS("Playlists"), HISTORY("History"), OFFLINE("Offline") }

data class OfflineSong(val song: Song, val path: String?)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val library: LibraryRepository,
    private val downloads: DownloadRepository,
) : ViewModel() {

    val favorites: StateFlow<List<Song>> =
        library.observeFavorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        library.observePlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<Song>> =
        library.observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offline: StateFlow<List<OfflineSong>> =
        downloads.observeCompleted()
            .combine(library.observeFavorites()) { completed, _ ->
                completed.map { OfflineSong(song = it.toDomain(), path = it.localPath) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() = viewModelScope.launch { library.clearHistory() }

    fun toggleFavorite(song: Song) = viewModelScope.launch { library.toggleFavorite(song) }
}
