package com.xrotune.music.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xrotune.music.domain.model.Song
import com.xrotune.music.ui.viewmodel.SongActionsViewModel

/**
 * Renders the song action sheet plus its follow-up dialogs (playlist picker /
 * playlist creation) and keeps them in sync with the shared view model.
 */
@Composable
fun SongActionHost(
    song: Song,
    isFavorite: Boolean,
    viewModel: SongActionsViewModel,
    onDismiss: () -> Unit,
    onViewSimilar: () -> Unit = {},
) {
    var showPlaylistPicker by remember { mutableStateOf(false) }
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    SongActionsSheet(
        song = song,
        isFavorite = isFavorite,
        onDismiss = onDismiss,
        onAddToPlaylist = { showPlaylistPicker = true },
        onDownload = { viewModel.download(song); onDismiss() },
        onToggleFavorite = { viewModel.toggleFavorite(song); onDismiss() },
        onShare = { viewModel.share(song); onDismiss() },
        onViewSimilar = { onDismiss(); onViewSimilar() },
    )

    if (showPlaylistPicker) {
        AddToPlaylistDialog(
            playlists = playlists.map { it.id to it.name },
            onDismiss = { showPlaylistPicker = false },
            onPick = { id ->
                viewModel.addToPlaylist(id, song)
                showPlaylistPicker = false
                onDismiss()
            },
            onCreate = { name ->
                viewModel.createPlaylistAndAdd(name, song)
                showPlaylistPicker = false
                onDismiss()
            },
        )
    }
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    onCreate: (String) -> Unit,
) {
    var creating by remember { mutableStateOf(playlists.isEmpty()) }
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (creating) "New playlist" else "Add to playlist") },
        text = {
            if (creating) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            } else {
                Column {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(playlists) { (id, name) ->
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                                    .clickable { onPick(id) },
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { creating = true }) { Text("+ Create new playlist") }
                }
            }
        },
        confirmButton = {
            if (creating) {
                TextButton(
                    onClick = { if (newName.isNotBlank()) onCreate(newName.trim()) },
                ) { Text("Create & add") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        dismissButton = if (creating && playlists.isNotEmpty()) {
            { TextButton(onClick = { creating = false }) { Text("Back") } }
        } else null,
    )
}
