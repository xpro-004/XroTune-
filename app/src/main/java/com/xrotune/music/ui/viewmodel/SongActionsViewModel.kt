package com.xrotune.music.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.DownloadRepository
import com.xrotune.music.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the "add to playlist / download / favourite / share" bottom sheet. */
@HiltViewModel
class SongActionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val library: LibraryRepository,
    private val downloads: DownloadRepository,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> =
        library.observePlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<Set<String>> =
        library.observeFavorites()
            .map { songs -> songs.map { it.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    fun addToPlaylist(playlistId: Long, song: Song) = viewModelScope.launch {
        library.addToPlaylist(playlistId, song)
        _messages.emit("Added to playlist")
    }

    fun createPlaylistAndAdd(name: String, song: Song) = viewModelScope.launch {
        val id = library.createPlaylist(name)
        library.addToPlaylist(id, song)
        _messages.emit("Created \"$name\" and added song")
    }

    fun download(song: Song) = viewModelScope.launch {
        downloads.enqueue(song)
        _messages.emit("Download started")
    }

    fun toggleFavorite(song: Song) = viewModelScope.launch {
        val added = library.toggleFavorite(song)
        _messages.emit(if (added) "Added to favourites" else "Removed from favourites")
    }

    fun share(song: Song) {
        val url = song.streamUrl ?: "https://play.google.com/store/apps/details?id=com.xrotune.music"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, song.title)
            putExtra(Intent.EXTRA_TEXT, "${song.title} — ${song.artist}\n$url")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }
}
