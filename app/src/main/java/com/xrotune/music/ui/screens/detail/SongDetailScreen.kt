package com.xrotune.music.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xrotune.music.core.common.Formatters
import com.xrotune.music.domain.model.Song
import com.xrotune.music.ui.components.EmptyState
import com.xrotune.music.ui.components.ErrorState
import com.xrotune.music.ui.components.LoadingBox
import com.xrotune.music.ui.components.SectionHeader
import com.xrotune.music.ui.components.SongActionHost
import com.xrotune.music.ui.components.SongRow
import com.xrotune.music.ui.viewmodel.PlayerViewModel
import com.xrotune.music.ui.viewmodel.SongActionsViewModel

@Composable
fun SongDetailScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: SongDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    actionsViewModel: SongActionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
    val favoriteIds by actionsViewModel.favoriteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showActions by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingBox()
            state.song == null -> ErrorState(state.error ?: "Song unavailable", onRetry = viewModel::load)
            else -> {
                val song = state.song!!
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    item { DetailHeader(song, onBack, onOpenPlayer) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    playerViewModel.play(song, listOf(song) + state.similar)
                                    viewModel.onPlayed(song)
                                    onOpenPlayer()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Play")
                            }
                            CircleAction(Icons.Default.PlaylistAdd) { showActions = true }
                            CircleAction(Icons.Default.Download) {
                                actionsViewModel.download(song)
                            }
                            CircleAction(Icons.Default.Share) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${song.title} — ${song.artist}\n${song.streamUrl ?: ""}")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share"))
                            }
                        }
                    }
                    item { SectionHeader("Similar songs") }
                    if (state.similar.isEmpty()) {
                        item {
                            Text(
                                "No similar songs found.",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(state.similar, key = { it.id }) { similar ->
                            SongRow(
                                song = similar,
                                isCurrent = currentSong?.id == similar.id,
                                onClick = {
                                    playerViewModel.play(similar, state.similar)
                                    viewModel.onPlayed(similar)
                                },
                                onMore = { showActions = true },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showActions && state.song != null) {
        val song = state.song!!
        SongActionHost(
            song = song,
            isFavorite = favoriteIds.contains(song.id),
            viewModel = actionsViewModel,
            onDismiss = { showActions = false },
            onViewSimilar = { /* similar list is already on this screen */ },
        )
    }
}

@Composable
private fun DetailHeader(song: Song, onBack: () -> Unit, onOpenPlayer: () -> Unit) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.1f)) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.15f), Color(0xCC0B0B12)),
                        ),
                    ),
            )
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                song.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(song.artist, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (song.playCount > 0) Stat("Plays", Formatters.count(song.playCount))
                if (song.likeCount > 0) Stat("Likes", Formatters.count(song.likeCount))
                if (song.durationSeconds > 0) Stat("Duration", Formatters.duration(song.durationSeconds))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CircleAction(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null)
    }
}
