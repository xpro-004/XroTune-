package com.xrotune.music.data.remote.dto

import com.xrotune.music.domain.model.Banner
import com.xrotune.music.domain.model.Song
import com.xrotune.music.domain.model.User

fun MediaItemDto.toDomain(): Song = Song(
    id = itemId ?: id ?: url ?: hashCode().toString(),
    title = title ?: name ?: "Unknown title",
    artist = author?.nickName ?: "Unknown artist",
    coverUrl = imgBig ?: img ?: cover,
    streamUrl = playUrl ?: videoUrl ?: url,
    durationSeconds = parseDuration(duration),
    ctype = ctype ?: "video",
    encryptType = encryptType,
    playCount = view,
    likeCount = like,
    downloadCount = download,
    shareCount = share,
    authorId = author?.id ?: 0,
    authorAvatar = author?.avatarUrl,
    publishTime = ptime,
    language = lang,
)

fun EntryDetailResponse.toDomain(): Song = Song(
    id = itemId ?: hashCode().toString(),
    title = title ?: "Unknown title",
    artist = author?.nickName ?: "Unknown artist",
    coverUrl = imgBig ?: img,
    streamUrl = playUrl ?: playUrls?.firstOrNull { !it.url.isNullOrBlank() }?.url,
    durationSeconds = parseDuration(duration),
    ctype = ctype ?: "video",
    encryptType = encryptType,
    playCount = view,
    likeCount = like,
    downloadCount = download,
    shareCount = share,
    authorId = author?.id ?: 0,
    authorAvatar = author?.avatarUrl,
    language = null,
)

fun BannerDto.toDomain(): Banner = Banner(
    id = id ?: img ?: image ?: hashCode().toString(),
    title = title ?: "",
    imageUrl = image ?: img,
    targetUrl = jumpUrl ?: url,
)

fun UserInfoDto.toDomain(): User = User(
    uid = uid ?: id.toString(),
    nickname = nickName ?: nickNameAlt ?: "XroTune User",
    avatarUrl = avatarUrl ?: avatar,
    phone = phone ?: mobile,
    token = token ?: "",
)

/** Accepts "12", "12.5" or "mm:ss" and returns seconds. */
private fun parseDuration(raw: String?): Long {
    if (raw.isNullOrBlank()) return 0
    raw.toLongOrNull()?.let { return it }
    raw.toDoubleOrNull()?.let { return it.toLong() }
    val parts = raw.split(":")
    return when (parts.size) {
        2 -> (parts[0].toLongOrNull() ?: 0) * 60 + (parts[1].toLongOrNull() ?: 0)
        3 -> (parts[0].toLongOrNull() ?: 0) * 3600 + (parts[1].toLongOrNull() ?: 0) * 60 + (parts[2].toLongOrNull() ?: 0)
        else -> 0
    }
}
