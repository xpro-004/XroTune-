package com.xrotune.music.domain.repository

import com.xrotune.music.core.common.Result
import com.xrotune.music.domain.model.AppEntry
import com.xrotune.music.domain.model.Banner
import com.xrotune.music.domain.model.Song

interface MusicRepository {
    suspend fun search(keyword: String, page: Int = 1): Result<List<Song>>
    suspend fun trending(page: Int = 1): Result<List<Song>>
    suspend fun banners(): Result<List<Banner>>
    suspend fun promotedApps(): Result<List<AppEntry>>
    suspend fun songDetail(id: String): Result<Song>
    suspend fun similar(id: String, type: String): Result<List<Song>>
    suspend fun reportPlay(song: Song)
}
