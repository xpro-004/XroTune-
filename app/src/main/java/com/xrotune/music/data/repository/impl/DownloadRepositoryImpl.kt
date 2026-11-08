package com.xrotune.music.data.repository.impl

import android.content.Context
import com.xrotune.music.core.crypto.Secrets
import com.xrotune.music.core.crypto.XorCrypto
import com.xrotune.music.data.local.db.dao.DownloadDao
import com.xrotune.music.data.local.db.entity.DownloadEntity
import com.xrotune.music.data.local.db.entity.DownloadStatus
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads songs to internal storage. Native streams (encryptType != 0) are
 * stored XOR-decrypted so they can be played directly offline by ExoPlayer.
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
) : DownloadRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    /**
     * Media files come from CDNs, not the API host, so they must not carry the
     * API signature/params — hence a dedicated, plain client.
     */
    private val mediaClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val downloadDir: File
        get() = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

    override fun observeDownloads(): Flow<List<DownloadEntity>> = downloadDao.observeAll()

    override fun observeCompleted(): Flow<List<DownloadEntity>> = downloadDao.observeCompleted()

    override suspend fun get(songId: String): DownloadEntity? = downloadDao.findById(songId)

    override suspend fun enqueue(song: Song) = withContext(Dispatchers.IO) {
        val url = song.streamUrl ?: return@withContext
        downloadDao.upsert(
            DownloadEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                coverUrl = song.coverUrl,
                remoteUrl = url,
                status = DownloadStatus.QUEUED.name,
                encryptType = song.encryptType,
            ),
        )
        jobs[song.id]?.cancel()
        jobs[song.id] = scope.launch { runDownload(song) }
    }

    private suspend fun runDownload(song: Song) {
        val url = song.streamUrl ?: return
        val target = File(downloadDir, "${sanitize(song.id)}.${extension(url)}")
        try {
            downloadDao.updateProgress(song.id, 0, DownloadStatus.DOWNLOADING.name)
            val request = Request.Builder().url(url).build()
            mediaClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body ?: error("Empty body")
                val total = body.contentLength().coerceAtLeast(0)
                val encryptType = song.encryptType
                val key = Secrets.xorKeyForType(encryptType)

                body.byteStream().use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var read = input.read(buffer)
                        var written = 0L
                        while (read > 0) {
                            if (encryptType != 0) {
                                val decrypted = XorCrypto.decrypt(buffer.copyOf(read), key)
                                output.write(decrypted, 0, read)
                            } else {
                                output.write(buffer, 0, read)
                            }
                            written += read
                            downloadDao.updateProgress(song.id, written, DownloadStatus.DOWNLOADING.name)
                            read = input.read(buffer)
                        }
                        output.flush()
                    }
                }
                downloadDao.upsert(
                    DownloadEntity(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        coverUrl = song.coverUrl,
                        remoteUrl = url,
                        localPath = target.absolutePath,
                        status = DownloadStatus.COMPLETED.name,
                        downloadedBytes = target.length(),
                        totalBytes = target.length(),
                        encryptType = 0,
                    ),
                )
            }
        } catch (t: Throwable) {
            runCatching { target.delete() }
            downloadDao.upsert(
                DownloadEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    coverUrl = song.coverUrl,
                    remoteUrl = url,
                    status = DownloadStatus.FAILED.name,
                    error = t.message,
                    encryptType = song.encryptType,
                ),
            )
        } finally {
            jobs.remove(song.id)
        }
    }

    override suspend fun cancel(songId: String): Unit = withContext(Dispatchers.IO) {
        jobs[songId]?.cancel()
        jobs.remove(songId)
        downloadDao.findById(songId)?.let { downloadDao.upsert(it.copy(status = DownloadStatus.PAUSED.name)) }
    }

    override suspend fun remove(songId: String): Unit = withContext(Dispatchers.IO) {
        jobs[songId]?.cancel()
        jobs.remove(songId)
        downloadDao.findById(songId)?.localPath?.let { path -> runCatching { File(path).delete() } }
        downloadDao.delete(songId)
    }

    override suspend fun clearAll(): Unit = withContext(Dispatchers.IO) {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        downloadDir.listFiles()?.forEach { it.delete() }
        downloadDao.clear()
    }

    private fun sanitize(name: String) = name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(64)

    private fun extension(url: String): String {
        val path = url.substringBefore('?')
        val ext = path.substringAfterLast('.', "")
        return if (ext.length in 2..4) ext else "mp4"
    }
}
