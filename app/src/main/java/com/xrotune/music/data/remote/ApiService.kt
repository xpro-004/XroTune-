package com.xrotune.music.data.remote

import com.xrotune.music.data.remote.dto.ApiEnvelope
import com.xrotune.music.data.remote.dto.AppsTabResponse
import com.xrotune.music.data.remote.dto.BannerResponse
import com.xrotune.music.data.remote.dto.BtInfoResponse
import com.xrotune.music.data.remote.dto.EntryDetailResponse
import com.xrotune.music.data.remote.dto.MediaItemDto
import com.xrotune.music.data.remote.dto.MomentResponse
import com.xrotune.music.data.remote.dto.PrepareLoginResponse
import com.xrotune.music.data.remote.dto.RowResponse
import com.xrotune.music.data.remote.dto.SearchResponse
import com.xrotune.music.data.remote.dto.SendCodeResponse
import com.xrotune.music.data.remote.dto.SimpleResponse
import com.xrotune.music.data.remote.dto.UpdateCheckResponse
import com.xrotune.music.data.remote.dto.UserInfoDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * All backend calls. Common params (lang/country/ver_code/...) and the request
 * signature are added automatically by [com.xrotune.music.data.remote.interceptor.SignInterceptor],
 * and every response is transparently decrypted by
 * [com.xrotune.music.data.remote.interceptor.DecryptInterceptor].
 */
interface ApiService {

    // ---------------- Search ----------------
    @GET("/api/search/all")
    suspend fun searchAll(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
    ): ApiEnvelope<SearchResponse>

    @GET("/api/search/picture")
    suspend fun searchPicture(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    @GET("/api/search/status")
    suspend fun searchStatus(
        @Query("keyword") keyword: String,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    // ---------------- Entry / Recommendation ----------------
    @GET("/api/entry/reco/detail")
    suspend fun entryRecoDetail(
        @Query("entry_id") entryId: String,
        @Query("type") type: String? = null,
    ): ApiEnvelope<EntryDetailResponse>

    @GET("/api/entry/reco/moment")
    suspend fun entryRecoMoment(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
        @Query("type") type: String? = null,
    ): ApiEnvelope<MomentResponse>

    @GET("/api/entry/reco/similar")
    suspend fun entryRecoSimilar(
        @Query("entry_id") entryId: String,
        @Query("type") type: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    @GET("/api/entry/reco/similar_by_page")
    suspend fun entryRecoSimilarByPage(
        @Query("entry_id") entryId: String,
        @Query("type") type: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    @GET("/api/entry/action/{action}")
    suspend fun entryAction(
        @Path("action") action: String,
        @Query("item_id") itemId: String,
        @Query("type") type: String? = null,
    ): ApiEnvelope<SimpleResponse>

    // ---------------- CMS Flow ----------------
    @GET("/api/cmsflow/apps_tab/get")
    suspend fun appsTab(): ApiEnvelope<AppsTabResponse>

    @GET("/api/cmsflow/banner/get")
    suspend fun bannerGet(
        @Query("position") position: String? = null,
        @Query("type") type: String? = null,
    ): ApiEnvelope<BannerResponse>

    @GET("/api/cmsflow/config/config_v3")
    suspend fun configV3(): ApiEnvelope<Map<String, Any>>

    @GET("/api/cmsflow/report/add")
    suspend fun reportAdd(
        @Query("item_id") itemId: String,
        @Query("reason") reason: String? = null,
    ): ApiEnvelope<SimpleResponse>

    // ---------------- User ----------------
    @GET("/api/user/login/prepare_login")
    suspend fun prepareLogin(
        @Query("phone") phone: String? = null,
        @Query("mobile") mobile: String? = null,
    ): ApiEnvelope<PrepareLoginResponse>

    @GET("/api/user/login/pre_bind")
    suspend fun preBind(
        @Query("phone") phone: String,
    ): ApiEnvelope<PrepareLoginResponse>

    @GET("/api/user/login/bind")
    suspend fun bind(
        @Query("phone") phone: String,
        @Query("code") code: String,
    ): ApiEnvelope<UserInfoDto>

    @GET("/api/user/login/submit")
    suspend fun submitLogin(
        @Query("phone") phone: String,
        @Query("code") code: String,
        @Query("token") token: String? = null,
    ): ApiEnvelope<UserInfoDto>

    @GET("/api/user/sms/send_code")
    suspend fun sendSmsCode(
        @Query("phone") phone: String,
        @Query("type") type: Int = 1,
    ): ApiEnvelope<SendCodeResponse>

    @GET("/api/user/content/video")
    suspend fun userContentVideo(
        @Query("uid") uid: String,
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 20,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    @GET("/api/user/setter/{action}")
    suspend fun userSetter(
        @Path("action") action: String,
        @Query("value") value: String? = null,
    ): ApiEnvelope<SimpleResponse>

    @GET("/user/update")
    suspend fun userUpdate(
        @Query("nickName") nickName: String? = null,
        @Query("signature") signature: String? = null,
        @Query("avatar") avatar: String? = null,
    ): ApiEnvelope<UserInfoDto>

    // ---------------- Misc / Movie / BT ----------------
    @GET("/api/misc/moviev3/bt_info")
    suspend fun movieBtInfo(
        @Query("item_id") itemId: String,
        @Query("type") type: String? = null,
    ): ApiEnvelope<BtInfoResponse>

    @GET("/api/misc/moviev3/bt_torrent")
    suspend fun movieBtTorrent(
        @Query("item_id") itemId: String,
        @Query("info_hash") infoHash: String? = null,
    ): ApiEnvelope<BtInfoResponse>

    // ---------------- Prism ----------------
    @POST("/api/prism/bt/upload")
    suspend fun prismBtUpload(): ApiEnvelope<SimpleResponse>

    @POST("/api/prism/task/upload")
    suspend fun prismTaskUpload(): ApiEnvelope<SimpleResponse>

    @GET("/api/prism/tracker/get")
    suspend fun prismTrackerGet(): ApiEnvelope<Map<String, Any>>

    @POST("/api/prism/tracker/upload")
    suspend fun prismTrackerUpload(): ApiEnvelope<SimpleResponse>

    // ---------------- Nemo Activity ----------------
    @GET("/api/nemo_activity/activity/banner")
    suspend fun nemoBanner(): ApiEnvelope<BannerResponse>

    @GET("/api/nemo_activity/task/list")
    suspend fun nemoTaskList(): ApiEnvelope<RowResponse<MediaItemDto>>

    @GET("/api/nemo_activity/user/{action}")
    suspend fun nemoUser(@Path("action") action: String): ApiEnvelope<Map<String, Any>>

    // ---------------- Device / Update ----------------
    @GET("/api/nemo_device/silent/get")
    suspend fun deviceSilentGet(): ApiEnvelope<Map<String, Any>>

    @GET("/api/dev/update/check")
    suspend fun updateCheck(): ApiEnvelope<UpdateCheckResponse>

    // ---------------- Appstore ----------------
    @GET("/api/appstore_api/update/check_increment")
    suspend fun appstoreCheckIncrement(): ApiEnvelope<UpdateCheckResponse>

    @GET("/api/appstore_api/vdm/hight_quality")
    suspend fun highQuality(
        @Query("item_id") itemId: String? = null,
    ): ApiEnvelope<RowResponse<MediaItemDto>>

    // ---------------- Parser / M3U8 ----------------
    @GET("/api/parser/tick/front/7000")
    suspend fun parserTick(): ApiEnvelope<SimpleResponse>

    @GET
    suspend fun m3u8VideoUrl(@Url url: String): ApiEnvelope<Map<String, Any>>

    // ---------------- Auth ----------------
    @GET("/auth/token/refresh")
    suspend fun refreshToken(
        @Query("token") token: String,
    ): ApiEnvelope<UserInfoDto>
}
