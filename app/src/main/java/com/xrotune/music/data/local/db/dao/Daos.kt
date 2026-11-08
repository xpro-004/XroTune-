package com.xrotune.music.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.data.local.db.entity.HistoryEntity
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.data.local.db.entity.PlaylistSongEntity
import com.xrotune.music.data.local.db.entity.SearchHistoryEntity
import com.xrotune.music.data.local.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): SongEntity?

    @Query("SELECT * FROM songs")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("UPDATE songs SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)
}

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(ref: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSong(playlistId: Long, songId: String)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC, addedAt ASC")
    fun observeSongRefs(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query(
        "SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId " +
            "WHERE ps.playlistId = :playlistId ORDER BY ps.position ASC, ps.addedAt ASC"
    )
    fun observeSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query(
        "SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId " +
            "WHERE ps.playlistId = :playlistId ORDER BY ps.position ASC, ps.addedAt ASC"
    )
    suspend fun songs(playlistId: Long): List<SongEntity>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun observeSongCount(playlistId: Long): Flow<Int>

    @Query("UPDATE playlists SET updatedAt = :time WHERE id = :playlistId")
    suspend fun touch(playlistId: Long, time: Long = System.currentTimeMillis())
}

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    fun observeCompleted(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE songId = :songId LIMIT 1")
    suspend fun findById(songId: String): DownloadEntity?

    @Query("UPDATE downloads SET downloadedBytes = :bytes, status = :status, updatedAt = :time WHERE songId = :songId")
    suspend fun updateProgress(songId: String, bytes: Long, status: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("DELETE FROM downloads")
    suspend fun clear()
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun clear()

    @Query("DELETE FROM history WHERE songId = :songId")
    suspend fun remove(songId: String)
}

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun observeRecent(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history")
    suspend fun clear()
}
