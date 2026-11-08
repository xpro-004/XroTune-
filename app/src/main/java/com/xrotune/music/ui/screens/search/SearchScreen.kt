package com.xrotune.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xrotune.music.ui.components.EmptyState
import com.xrotune.music.ui.components.SongRow
import com.xrotune.music.ui.viewmodel.PlayerViewModel

@Composable
fun SearchScreen(
    onSongClick: (String) -> Unit,
    onPlayerClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    actionsViewModel: com.xrotune.music.ui.viewmodel.SongActionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val favoriteIds by actionsViewModel.favoriteIds.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    var actionSongId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search songs, artists, videos") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Row {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                    }
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                viewModel.submit()
            }),
        )

        Spacer(Modifier.height(8.dp))

        when {
            state.query.isBlank() -> HistoryList(history, onClear = viewModel::clearHistory, onClick = viewModel::searchKeyword)
            state.hasSearched && state.results.isEmpty() && !state.isLoading ->
                EmptyState("No results", "Try a different keyword.")
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(state.results, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isCurrent = playerViewModel.currentSong.collectAsStateWithLifecycle().value?.id == song.id,
                        onClick = {
                            playerViewModel.play(song, state.results)
                            onSongClick(song.id)
                        },
                        onMore = { actionSongId = song.id },
                    )
                }
            }
        }
    }

    // Song options sheet, shared with the rest of the app.
    actionSongId?.let { id ->
        val song = state.results.firstOrNull { it.id == id }
        if (song != null) {
            com.xrotune.music.ui.components.SongActionHost(
                song = song,
                isFavorite = favoriteIds.contains(song.id),
                viewModel = actionsViewModel,
                onDismiss = { actionSongId = null },
            )
        }
    }
}

@Composable
private fun HistoryList(history: List<String>, onClear: () -> Unit, onClick: (String) -> Unit) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState("Search XroTune", "Find songs, videos and artists.")
        }
        return
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recent searches", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClear) { Text("Clear") }
        }
        LazyColumn {
            items(history) { keyword ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Text(
                        keyword,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().clickable { onClick(keyword) },
                    )
                }
            }
        }
    }
}
