package com.xrotune.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.xrotune.music.data.player.PlayerController
import com.xrotune.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Thin, app-wide facade over [PlayerController] so any screen (mini player,
 * full player, song rows) can observe and drive playback.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
) : ViewModel() {

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val position: StateFlow<Long> = controller.position
    val duration: StateFlow<Long> = controller.duration
    val queue: StateFlow<List<Song>> = controller.queue
    val currentIndex: StateFlow<Int> = controller.currentIndex
    val buffering: StateFlow<Boolean> = controller.buffering
    val shuffle: StateFlow<Boolean> = controller.shuffle
    val repeatMode: StateFlow<com.xrotune.music.data.player.RepeatMode> = controller.repeatMode

    fun play(song: Song, queue: List<Song> = listOf(song)) = controller.playSong(song, queue)
    fun playQueue(songs: List<Song>, startIndex: Int = 0) = controller.playQueue(songs, startIndex)
    fun addToQueue(song: Song) = controller.addToQueue(song)
    fun togglePlayPause() = controller.togglePlayPause()
    fun next() = controller.next()
    fun previous() = controller.previous()
    fun seekTo(ms: Long) = controller.seekTo(ms)
    fun seekToIndex(index: Int) = controller.seekToIndex(index)
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeat() = controller.cycleRepeat()
}
