package com.xrotune.music.ui.screens.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.domain.model.Song
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
class PlaylistsViewModel @Inject constructor(
    private val library: LibraryRepository,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> =
        library.observePlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    fun create(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        library.createPlaylist(name.trim())
        _messages.emit("Playlist created")
    }

    fun rename(id: Long, name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        library.renamePlaylist(id, name.trim())
    }

    fun delete(id: Long) = viewModelScope.launch {
        library.deletePlaylist(id)
        _messages.emit("Playlist deleted")
    }
}

data class PlaylistDetailState(
    val name: String = "",
    val songs: List<Song> = emptyList(),
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val library: LibraryRepository,
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: 0L

    val songs: StateFlow<List<Song>> =
        library.observePlaylistSongs(playlistId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _name = kotlinx.coroutines.flow.MutableStateFlow("")
    val name: StateFlow<String> = _name

    init {
        viewModelScope.launch { _name.value = library.findPlaylist(playlistId)?.name ?: "Playlist" }
    }

    fun remove(songId: String) = viewModelScope.launch { library.removeFromPlaylist(playlistId, songId) }

    fun rename(newName: String) = viewModelScope.launch {
        library.renamePlaylist(playlistId, newName)
        _name.value = newName
    }
}
