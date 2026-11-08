package com.xrotune.music.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xrotune.music.data.local.db.dao.DownloadDao
import com.xrotune.music.data.local.db.dao.HistoryDao
import com.xrotune.music.data.local.db.dao.PlaylistDao
import com.xrotune.music.data.local.db.dao.SearchHistoryDao
import com.xrotune.music.data.local.db.dao.SongDao
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.data.local.db.entity.HistoryEntity
import com.xrotune.music.data.local.db.entity.PlaylistEntity
import com.xrotune.music.data.local.db.entity.PlaylistSongEntity
import com.xrotune.music.data.local.db.entity.SearchHistoryEntity
import com.xrotune.music.data.local.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        DownloadEntity::class,
        HistoryEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class XroTuneDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val NAME = "xrotune.db"
    }
}
