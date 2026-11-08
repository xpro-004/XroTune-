package com.xrotune.music.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Envelope returned by every endpoint after the decrypt interceptor runs. */
data class ApiEnvelope<T>(
    @SerializedName("status") val status: Int = -1,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("data") val data: T? = null,
) {
    val isSuccess: Boolean get() = status == 1 || status == 0
}

/** Generic list wrapper used by most content endpoints. */
data class RowResponse<T>(
    @SerializedName("row_count") val rowCount: Int = 0,
    @SerializedName("rows") val rows: List<T>? = null,
    @SerializedName("abtag") val abtag: String? = null,
    @SerializedName("next_page") val nextPage: Int? = null,
    @SerializedName("has_more") val hasMore: Int? = null,
)

data class SearchResponse(
    @SerializedName("meta") val meta: String? = null,
    @SerializedName("slices") val slices: List<Slice>? = null,
)

data class Slice(
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("rows") val rows: List<MediaItemDto>? = null,
)

data class MediaItemDto(
    @SerializedName("item_id") val itemId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("img") val img: String? = null,
    @SerializedName("img_big") val imgBig: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("ctype") val ctype: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("play_url") val playUrl: String? = null,
    @SerializedName("video_url") val videoUrl: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("author") val author: AuthorDto? = null,
    @SerializedName("view") val view: Long = 0,
    @SerializedName("like") val like: Long = 0,
    @SerializedName("download") val download: Long = 0,
    @SerializedName("share") val share: Long = 0,
    @SerializedName("ptime") val ptime: Long = 0,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("source") val source: Int = 0,
    @SerializedName("encrypt_type") val encryptType: Int = 0,
)

data class AuthorDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("nickName") val nickName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("follow") val follow: Boolean = false,
)

data class BannerDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("img") val img: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("jump_url") val jumpUrl: String? = null,
    @SerializedName("type") val type: String? = null,
)

data class BannerResponse(
    @SerializedName("banners") val banners: List<BannerDto>? = null,
    @SerializedName("rows") val rows: List<BannerDto>? = null,
)

data class TabDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("tab_id") val tabId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("type") val type: String? = null,
)

data class AppsTabResponse(
    @SerializedName("titles") val titles: List<TabDto>? = null,
    @SerializedName("categorys") val categorys: List<TabDto>? = null,
    @SerializedName("apps") val apps: List<AppDto>? = null,
    @SerializedName("adds") val adds: List<AppDto>? = null,
    @SerializedName("ip") val ip: String? = null,
)

data class AppDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("packageName") val packageName: String? = null,
    @SerializedName("downloadAddress") val downloadAddress: String? = null,
    @SerializedName("rate") val rate: Double = 0.0,
)

data class EntryDetailResponse(
    @SerializedName("item_id") val itemId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("img") val img: String? = null,
    @SerializedName("img_big") val imgBig: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("play_url") val playUrl: String? = null,
    @SerializedName("play_urls") val playUrls: List<PlayUrlDto>? = null,
    @SerializedName("author") val author: AuthorDto? = null,
    @SerializedName("view") val view: Long = 0,
    @SerializedName("like") val like: Long = 0,
    @SerializedName("download") val download: Long = 0,
    @SerializedName("share") val share: Long = 0,
    @SerializedName("ctype") val ctype: String? = null,
    @SerializedName("encrypt_type") val encryptType: Int = 0,
    @SerializedName("desc") val desc: String? = null,
)

data class PlayUrlDto(
    @SerializedName("url") val url: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("type") val type: Int = 0,
    @SerializedName("encrypt_type") val encryptType: Int = 0,
    @SerializedName("size") val size: Long = 0,
)

data class MomentResponse(
    @SerializedName("items") val items: List<MediaItemDto>? = null,
)

data class UserInfoDto(
    @SerializedName("uid") val uid: String? = null,
    @SerializedName("id") val id: Long = 0,
    @SerializedName("nickName") val nickName: String? = null,
    @SerializedName("nick_name") val nickNameAlt: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("signature") val signature: String? = null,
    @SerializedName("sex") val sex: Int = 0,
)

data class PrepareLoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("prepare_token") val prepareToken: String? = null,
    @SerializedName("need_captcha") val needCaptcha: Int = 0,
    @SerializedName("sms_sign") val smsSign: String? = null,
)

data class SendCodeResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("expire") val expire: Int = 0,
    @SerializedName("retry") val retry: Int = 0,
)

data class BtInfoResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("info_hash") val infoHash: String? = null,
    @SerializedName("torrent_url") val torrentUrl: String? = null,
    @SerializedName("file_size") val fileSize: Long = 0,
)

data class UpdateCheckResponse(
    @SerializedName("version_code") val versionCode: Int = 0,
    @SerializedName("version_name") val versionName: String? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("force_update") val forceUpdate: Int = 0,
    @SerializedName("update_desc") val updateDesc: String? = null,
)

data class SimpleResponse(
    @SerializedName("result") val result: Int = 0,
    @SerializedName("success") val success: Boolean = false,
)
