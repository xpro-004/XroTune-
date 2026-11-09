package com.xrotune.music.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xrotune.music.ui.components.EmptyState
import com.xrotune.music.ui.components.SongRow
import com.xrotune.music.ui.theme.XroPink
import com.xrotune.music.ui.theme.XroPurple
import com.xrotune.music.ui.viewmodel.PlayerViewModel

@Composable
fun LibraryScreen(
    onSongClick: (String) -> Unit,
    onPlaylistsClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onPlayerClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val offline by viewModel.offline.collectAsStateWithLifecycle()
    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
    val tabs = LibraryTab.entries

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Your Library",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 12.dp) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == tabIndex,
                    onClick = { tabIndex = index },
                    text = { Text(tab.label) },
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (tabs[tabIndex]) {
                LibraryTab.FAVOURITES -> {
                    if (favorites.isEmpty()) {
                        EmptyState("No favourites yet", "Tap the heart on any song to save it here.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(favorites, key = { it.id }) { song ->
                                SongRow(
                                    song = song,
                                    isCurrent = currentSong?.id == song.id,
                                    onClick = {
                                        playerViewModel.play(song, favorites)
                                        onSongClick(song.id)
                                    },
                                    onMore = { viewModel.toggleFavorite(song) },
                                )
                            }
                        }
                    }
                }

                LibraryTab.PLAYLISTS -> {
                    if (playlists.isEmpty()) {
                        EmptyState("No playlists yet", "Create a playlist to organise your music.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(playlists, key = { it.id }) { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onPlaylistsClick)
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Brush.linearGradient(listOf(XroPurple, XroPink))),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        playlist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.HISTORY -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                        ) {
                            if (history.isNotEmpty()) {
                                TextButton(onClick = viewModel::clearHistory) { Text("Clear history") }
                            }
                        }
                        if (history.isEmpty()) {
                            EmptyState("Nothing played yet", "Songs you play appear here.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                                items(history, key = { it.id }) { song ->
                                    SongRow(
                                        song = song,
                                        isCurrent = currentSong?.id == song.id,
                                        onClick = {
                                            playerViewModel.play(song, history)
                                            onSongClick(song.id)
                                        },
                                        onMore = { viewModel.toggleFavorite(song) },
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.OFFLINE -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                        ) {
                            TextButton(onClick = onDownloadsClick) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Download manager")
                            }
                        }
                        if (offline.isEmpty()) {
                            EmptyState("No downloads yet", "Downloaded songs play offline, without internet.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                                items(offline, key = { it.song.id }) { item ->
                                    SongRow(
                                        song = item.song,
                                        isCurrent = currentSong?.id == item.song.id,
                                        onClick = {
                                            playerViewModel.play(item.song, offline.map { it.song })
                                            onSongClick(item.song.id)
                                        },
                                        onMore = { viewModel.toggleFavorite(item.song) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
