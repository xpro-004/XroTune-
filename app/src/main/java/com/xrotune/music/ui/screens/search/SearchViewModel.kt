package com.xrotune.music.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.LibraryRepository
import com.xrotune.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Song> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    val history: StateFlow<List<String>> =
        library.observeSearchHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    /** Debounced, real-time search as the user types. */
    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), isLoading = false, hasSearched = false, error = null)
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            runSearch(query)
        }
    }

    fun submit() {
        val query = _state.value.query
        if (query.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch {
            library.addSearchHistory(query)
            runSearch(query)
        }
    }

    fun searchKeyword(keyword: String) {
        _state.value = _state.value.copy(query = keyword)
        submit()
    }

    private suspend fun runSearch(query: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, hasSearched = true)
        when (val result = repository.search(query)) {
            is com.xrotune.music.core.common.Result.Success ->
                _state.value = _state.value.copy(isLoading = false, results = result.data, error = null)
            is com.xrotune.music.core.common.Result.Failure ->
                _state.value = _state.value.copy(isLoading = false, results = emptyList(), error = result.error.message)
        }
    }

    fun clearHistory() = viewModelScope.launch { library.clearSearchHistory() }
}
