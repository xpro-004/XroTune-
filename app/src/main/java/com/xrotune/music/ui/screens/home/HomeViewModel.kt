package com.xrotune.music.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.core.common.Result
import com.xrotune.music.domain.model.Banner
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val banners: List<Banner> = emptyList(),
    val trending: List<Song> = emptyList(),
    val recommended: List<Song> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(isLoading = _state.value.trending.isEmpty(), error = null)
        viewModelScope.launch {
            val banners = repository.banners().getOrNull() ?: emptyList()
            val trending = repository.trending().getOrNull() ?: emptyList()
            val recommended = repository.search("music", page = 1).getOrNull() ?: emptyList()

            val error = if (trending.isEmpty() && recommended.isEmpty()) {
                "Could not reach XroTune servers. Check your connection and retry."
            } else null

            _state.value = HomeUiState(
                isLoading = false,
                isRefreshing = false,
                banners = banners,
                trending = trending,
                recommended = recommended.filterNot { rec -> trending.any { it.id == rec.id } },
                error = error,
            )
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isRefreshing = true)
        load()
    }
}
