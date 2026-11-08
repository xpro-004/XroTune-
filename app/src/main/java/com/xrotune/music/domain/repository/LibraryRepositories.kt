package com.xrotune.music.domain.repository

import com.xrotune.music.core.common.Result
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    // Playlists
    fun observePlaylists(): Flow<List<PlaylistEntity>>
    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>>
    fun observePlaylistCount(playlistId: Long): Flow<Int>
    suspend fun createPlaylist(name: String, description: String = ""): Long
    suspend fun renamePlaylist(playlistId: Long, name: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addToPlaylist(playlistId: Long, song: Song)
    suspend fun removeFromPlaylist(playlistId: Long, songId: String)
    suspend fun findPlaylist(playlistId: Long): PlaylistEntity?

    // Favorites
    fun observeFavorites(): Flow<List<Song>>
    suspend fun toggleFavorite(song: Song): Boolean

    // History
    fun observeHistory(): Flow<List<Song>>
    suspend fun addHistory(song: Song)
    suspend fun clearHistory()

    // Search history
    fun observeSearchHistory(): Flow<List<String>>
    suspend fun addSearchHistory(keyword: String)
    suspend fun clearSearchHistory()
}

interface DownloadRepository {
    fun observeDownloads(): Flow<List<DownloadEntity>>
    fun observeCompleted(): Flow<List<DownloadEntity>>
    suspend fun enqueue(song: Song)
    suspend fun cancel(songId: String)
    suspend fun remove(songId: String)
    suspend fun clearAll()
    suspend fun get(songId: String): DownloadEntity?
}

interface UserRepository {
    val isLoggedIn: Flow<Boolean>
    val user: Flow<com.xrotune.music.domain.model.User?>
    suspend fun sendCode(phone: String): Result<String>
    suspend fun verifyAndLogin(phone: String, code: String): Result<com.xrotune.music.domain.model.User>
    suspend fun logout()
}
