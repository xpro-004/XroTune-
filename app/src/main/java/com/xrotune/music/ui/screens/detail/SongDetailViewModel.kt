package com.xrotune.music.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.LibraryRepository
import com.xrotune.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = true,
    val song: Song? = null,
    val similar: List<Song> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val music: MusicRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    private val songId: String = savedStateHandle.get<String>("songId").orEmpty()

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    val history: StateFlow<List<Song>> =
        library.observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val detail = music.songDetail(songId)
            when (detail) {
                is com.xrotune.music.core.common.Result.Success -> {
                    val song = detail.data
                    val similar = music.similar(song.id, song.ctype).getOrNull() ?: emptyList()
                    _state.value = DetailUiState(isLoading = false, song = song, similar = similar)
                }
                is com.xrotune.music.core.common.Result.Failure -> {
                    // Fall back to history so the screen is still useful offline.
                    val cached = history.value.firstOrNull { it.id == songId }
                    _state.value = DetailUiState(
                        isLoading = false,
                        song = cached,
                        error = if (cached == null) detail.error.message else null,
                    )
                }
            }
        }
    }

    fun onPlayed(song: Song) = viewModelScope.launch {
        library.addHistory(song)
        music.reportPlay(song)
    }
}
