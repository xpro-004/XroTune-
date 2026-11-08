package com.xrotune.music.domain.model

/** Unified media/song model used throughout the UI layer. */
data class Song(
    val id: String,
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
) {
    val isStreamEncrypted: Boolean get() = encryptType != 0
}

data class Banner(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val targetUrl: String?,
)

data class AppEntry(
    val title: String,
    val iconUrl: String?,
    val packageName: String?,
    val downloadUrl: String?,
    val rating: Double = 0.0,
)

data class User(
    val uid: String,
    val nickname: String,
    val avatarUrl: String?,
    val phone: String?,
    val token: String,
) {
    val maskedPhone: String
        get() = phone?.let { if (it.length >= 4) "******" + it.takeLast(4) else it } ?: ""
}
