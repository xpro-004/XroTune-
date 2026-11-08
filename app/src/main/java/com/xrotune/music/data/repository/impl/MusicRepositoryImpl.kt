package com.xrotune.music.data.repository.impl

import com.xrotune.music.core.common.Result
import com.xrotune.music.core.common.runCatchingApp
import com.xrotune.music.data.remote.ApiService
import com.xrotune.music.data.remote.dto.toDomain
import com.xrotune.music.domain.model.AppEntry
import com.xrotune.music.domain.model.Banner
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : MusicRepository {

    /**
     * Small in-memory index of everything the user has seen. The detail endpoint
     * needs the item's original `ctype`, which is not part of the deep link, so
     * we resolve it from here and reuse it for offline fallbacks.
     */
    private val cache = java.util.concurrent.ConcurrentHashMap<String, Song>()

    private fun List<Song>.cached(): List<Song> = also { list -> list.forEach { cache[it.id] = it } }

    override suspend fun search(keyword: String, page: Int): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val fromAll = runCatching {
                api.searchAll(keyword = keyword, page = page)
                    .data?.slices
                    ?.flatMap { it.rows ?: emptyList() }
                    ?.map { it.toDomain() }
                    ?: emptyList()
            }.getOrDefault(emptyList())

            if (fromAll.isNotEmpty()) {
                fromAll.cached()
            } else {
                // The "all" endpoint only returns slice descriptors for some queries;
                // fall back to the richer picture/search endpoint.
                val rows = api.searchPicture(keyword = keyword, page = page).data?.rows ?: emptyList()
                if (rows.isEmpty()) throw com.xrotune.music.core.common.AppError.Empty()
                rows.map { it.toDomain() }.cached()
            }
        }
    }

    override suspend fun trending(page: Int): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val moments = runCatching {
                api.entryRecoMoment(page = page).data?.items?.map { it.toDomain() } ?: emptyList()
            }.getOrDefault(emptyList())

            if (moments.isNotEmpty()) {
                moments.cached()
            } else {
                // Fallback so Home always has fresh content.
                val rows = api.searchPicture(keyword = "music", page = page).data?.rows ?: emptyList()
                rows.map { it.toDomain() }.cached()
            }
        }
    }

    override suspend fun banners(): Result<List<Banner>> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val resp = api.bannerGet().data
            val list = resp?.banners ?: resp?.rows ?: emptyList()
            list.map { it.toDomain() }
        }
    }

    override suspend fun promotedApps(): Result<List<AppEntry>> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val resp = api.appsTab().data
            val apps = (resp?.apps ?: emptyList()) + (resp?.adds ?: emptyList())
            apps.map {
                AppEntry(
                    title = it.title ?: "",
                    iconUrl = it.icon,
                    packageName = it.packageName,
                    downloadUrl = it.downloadAddress,
                    rating = it.rate,
                )
            }
        }
    }

    override suspend fun songDetail(id: String): Result<Song> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val known = cache[id]
            val type = known?.ctype ?: "picture"
            val detail = runCatching { api.entryRecoDetail(entryId = id, type = type).data }.getOrNull()
            detail?.toDomain()?.cached()
                ?: known
                ?: throw com.xrotune.music.core.common.AppError.Empty()
        }
    }

    override suspend fun similar(id: String, type: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val byId = runCatching {
                api.entryRecoSimilar(entryId = id, type = type).data?.rows
            }.getOrNull()
                ?: runCatching { api.entryRecoSimilarByPage(entryId = id, type = type).data?.rows }.getOrNull()
                ?: emptyList()

            if (byId.isNotEmpty()) {
                byId.map { it.toDomain() }.cached()
            } else {
                // The similar endpoints are unsupported for some content types; fall
                // back to a keyword search so the section is never empty.
                val keyword = cache[id]?.title?.take(40) ?: return@runCatchingApp emptyList()
                val rows = runCatching { api.searchPicture(keyword = keyword).data?.rows }.getOrNull() ?: emptyList()
                rows.map { it.toDomain() }.filterNot { it.id == id }.cached()
            }
        }
    }

    private fun Song.cached(): Song = also { cache[it.id] = it }

    override suspend fun reportPlay(song: Song) {
        withContext(Dispatchers.IO) {
            runCatching { api.entryAction(action = "play", itemId = song.id, type = song.ctype) }
        }
    }
}
