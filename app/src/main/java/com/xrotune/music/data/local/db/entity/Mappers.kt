package com.xrotune.music.data.local.db.entity

import com.xrotune.music.domain.model.Song

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    streamUrl = streamUrl,
    durationSeconds = durationSeconds,
    ctype = ctype,
    encryptType = encryptType,
    playCount = playCount,
    likeCount = likeCount,
    downloadCount = downloadCount,
    shareCount = shareCount,
    authorId = authorId,
    authorAvatar = authorAvatar,
    publishTime = publishTime,
    language = language,
)

fun SongEntity.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    streamUrl = streamUrl,
    durationSeconds = durationSeconds,
    ctype = ctype,
    encryptType = encryptType,
    playCount = playCount,
    likeCount = likeCount,
    downloadCount = downloadCount,
    shareCount = shareCount,
    authorId = authorId,
    authorAvatar = authorAvatar,
    publishTime = publishTime,
    language = language,
)

fun HistoryEntity.toDomain(): Song = Song(
    id = songId,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    streamUrl = streamUrl,
)

fun DownloadEntity.toDomain(): Song = Song(
    id = songId,
    title = title,
    artist = artist,
    coverUrl = coverUrl,
    streamUrl = localPath ?: remoteUrl,
    encryptType = encryptType,
)
