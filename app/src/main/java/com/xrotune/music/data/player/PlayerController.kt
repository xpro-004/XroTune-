package com.xrotune.music.data.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.xrotune.music.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class RepeatMode { OFF, ALL, ONE }

/**
 * Owns the single ExoPlayer instance for the whole app and exposes observable
 * playback state to the UI. ExoPlayer is wrapped in a MediaSession by
 * [PlaybackService] so playback survives in the background.
 */
@OptIn(UnstableApi::class)
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext context: Context,
) {

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(XorAwareDataSourceFactory(context)))
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _buffering = MutableStateFlow(false)
    val buffering: StateFlow<Boolean> = _buffering.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _buffering.value = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncCurrent()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffle.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            }
        })

        scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _position.value = player.currentPosition.coerceAtLeast(0)
                    _duration.value = player.duration.coerceAtLeast(0)
                }
                delay(500)
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val resolvedQueue = if (queue.any { it.id == song.id }) queue else listOf(song) + queue
        _queue.value = resolvedQueue
        player.setMediaItems(resolvedQueue.map { it.toMediaItem() }, resolvedQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0), 0L)
        player.prepare()
        player.play()
        syncCurrent()
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        _queue.value = songs
        player.setMediaItems(songs.map { it.toMediaItem() }, startIndex.coerceIn(0, songs.lastIndex), 0L)
        player.prepare()
        player.play()
        syncCurrent()
    }

    fun addToQueue(song: Song) {
        val updated = _queue.value + song
        _queue.value = updated
        player.addMediaItem(song.toMediaItem())
    }

    fun removeFromQueue(songId: String) {
        val index = _queue.value.indexOfFirst { it.id == songId }
        if (index < 0) return
        _queue.value = _queue.value.filterNot { it.id == songId }
        player.removeMediaItem(index)
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem() else player.seekTo(0, 0L)
    }

    fun previous() {
        if (player.currentPosition > 3000) player.seekTo(0) else if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(0)
    }

    fun seekTo(ms: Long) {
        player.seekTo(ms.coerceIn(0, player.duration.coerceAtLeast(0)))
        _position.value = ms
    }

    fun seekToIndex(index: Int) {
        if (index in _queue.value.indices) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun cycleRepeat() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _queue.value = emptyList()
        _currentSong.value = null
        _currentIndex.value = -1
    }

    private fun syncCurrent() {
        val index = player.currentMediaItemIndex
        _currentIndex.value = index
        _currentSong.value = _queue.value.getOrNull(index)
    }

    private fun Song.toMediaItem(): MediaItem {
        val uri = streamUrl?.let { url ->
            when {
                encryptType != 0 -> XorAwareDataSourceFactory.buildUri(url, encryptType)
                url.startsWith("/") -> android.net.Uri.fromFile(java.io.File(url))
                else -> android.net.Uri.parse(url)
            }
        }
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(coverUrl?.let { android.net.Uri.parse(it) })
                    .build(),
            )
            .build()
    }
}
