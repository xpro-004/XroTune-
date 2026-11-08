package com.xrotune.music.ui.screens.downloads

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xrotune.music.core.common.Formatters
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.data.local.db.entity.DownloadStatus
import com.xrotune.music.data.local.db.entity.toDomain
import com.xrotune.music.ui.components.EmptyState
import com.xrotune.music.ui.viewmodel.PlayerViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DownloadManagerScreen(
    onBack: () -> Unit,
    onSongClick: (String) -> Unit,
    viewModel: DownloadManagerViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = viewModel::clearAll) { Text("Clear all") }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (items.isEmpty()) {
                EmptyState("No downloads", "Songs you download will show up here.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(items, key = { it.songId }) { item ->
                        DownloadRow(
                            item = item,
                            onPlay = {
                                val song = item.toDomain()
                                playerViewModel.play(song, listOf(song))
                                onSongClick(song.id)
                            },
                            onCancel = { viewModel.cancel(item.songId) },
                            onRemove = { viewModel.remove(item.songId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    item: DownloadEntity,
    onPlay: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
) {
    val status = runCatching { DownloadStatus.valueOf(item.status) }.getOrDefault(DownloadStatus.QUEUED)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AsyncImage(model = item.coverUrl, contentDescription = item.title, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    item.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIcon(status)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusLabel(item, status),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when (status) {
                DownloadStatus.COMPLETED -> IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
                }
                DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Cancel, contentDescription = "Pause")
                }
                else -> IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry")
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
        if (status == DownloadStatus.DOWNLOADING || status == DownloadStatus.QUEUED) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun StatusIcon(status: DownloadStatus) {
    val (icon, tint) = when (status) {
        DownloadStatus.COMPLETED -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        DownloadStatus.FAILED -> Icons.Default.Error to MaterialTheme.colorScheme.error
        else -> Icons.Default.Refresh to MaterialTheme.colorScheme.primary
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
}

private fun statusLabel(item: DownloadEntity, status: DownloadStatus): String = when (status) {
    DownloadStatus.COMPLETED -> "${Formatters.bytes(item.totalBytes)} • Downloaded"
    DownloadStatus.DOWNLOADING ->
        "${Formatters.bytes(item.downloadedBytes)} / ${Formatters.bytes(item.totalBytes)}"
    DownloadStatus.QUEUED -> "Queued"
    DownloadStatus.PAUSED -> "Paused"
    DownloadStatus.FAILED -> item.error ?: "Failed"
}
