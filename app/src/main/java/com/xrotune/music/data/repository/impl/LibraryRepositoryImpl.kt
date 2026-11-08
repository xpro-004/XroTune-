package com.xrotune.music.data.repository.impl

import com.xrotune.music.data.local.db.dao.HistoryDao
import com.xrotune.music.data.local.db.dao.PlaylistDao
import com.xrotune.music.data.local.db.dao.SearchHistoryDao
import com.xrotune.music.data.local.db.dao.SongDao
import com.xrotune.music.data.local.db.entity.HistoryEntity
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.data.local.db.entity.PlaylistSongEntity
import com.xrotune.music.data.local.db.entity.SearchHistoryEntity
import com.xrotune.music.data.local.db.entity.toDomain
import com.xrotune.music.data.local.db.entity.toEntity
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val historyDao: HistoryDao,
    private val searchHistoryDao: SearchHistoryDao,
) : LibraryRepository {

    override fun observePlaylists(): Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()

    override fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.observeSongs(playlistId).map { list -> list.map { it.toDomain() } }

    override fun observePlaylistCount(playlistId: Long): Flow<Int> = playlistDao.observeSongCount(playlistId)

    override suspend fun createPlaylist(name: String, description: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insert(PlaylistEntity(name = name, description = description))
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String): Unit = withContext(Dispatchers.IO) {
        playlistDao.findById(playlistId)?.let {
            playlistDao.update(it.copy(name = name, updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Unit = withContext(Dispatchers.IO) {
        playlistDao.findById(playlistId)?.let { playlistDao.delete(it) }
    }

    override suspend fun addToPlaylist(playlistId: Long, song: Song) = withContext(Dispatchers.IO) {
        songDao.upsert(song.toEntity())
        playlistDao.addSong(PlaylistSongEntity(playlistId = playlistId, songId = song.id))
        playlistDao.touch(playlistId)
    }

    override suspend fun removeFromPlaylist(playlistId: Long, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSong(playlistId, songId)
        playlistDao.touch(playlistId)
    }

    override suspend fun findPlaylist(playlistId: Long): PlaylistEntity? = withContext(Dispatchers.IO) {
        playlistDao.findById(playlistId)
    }

    override fun observeFavorites(): Flow<List<Song>> =
        songDao.observeAll().map { list -> list.filter { it.isFavorite }.map { it.toDomain() } }

    override suspend fun toggleFavorite(song: Song): Boolean = withContext(Dispatchers.IO) {
        songDao.upsert(song.toEntity())
        val current = songDao.findById(song.id)?.isFavorite ?: false
        val next = !current
        songDao.setFavorite(song.id, next)
        next
    }

    override fun observeHistory(): Flow<List<Song>> =
        historyDao.observeRecent().map { list -> list.map { it.toDomain() } }

    override suspend fun addHistory(song: Song) = withContext(Dispatchers.IO) {
        historyDao.upsert(
            HistoryEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                coverUrl = song.coverUrl,
                streamUrl = song.streamUrl,
            ),
        )
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) { historyDao.clear() }

    override fun observeSearchHistory(): Flow<List<String>> =
        searchHistoryDao.observeRecent().map { list -> list.map { it.keyword } }

    override suspend fun addSearchHistory(keyword: String) = withContext(Dispatchers.IO) {
        if (keyword.isNotBlank()) searchHistoryDao.upsert(SearchHistoryEntity(keyword.trim()))
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) { searchHistoryDao.clear() }
}
