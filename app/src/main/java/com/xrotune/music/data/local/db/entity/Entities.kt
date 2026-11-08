package com.xrotune.music.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val streamUrl: String?,
    val durationSeconds: Long = 0,
    val ctype: String = "video",
    val encryptType: Int = 0,
    val playCount: Long = 0,
    val likeCount: Long = 0,
    val downloadCount: Long = 0,
    val shareCount: Long = 0,
    val authorId: Long = 0,
    val authorAvatar: String? = null,
    val publishTime: Long = 0,
    val language: String? = null,
    val isFavorite: Boolean = false,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")],
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
)

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val remoteUrl: String?,
    val localPath: String? = null,
    val status: String = DownloadStatus.QUEUED.name,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val encryptType: Int = 0,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val progress: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val streamUrl: String?,
    val playedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val keyword: String,
    val searchedAt: Long = System.currentTimeMillis(),
)
