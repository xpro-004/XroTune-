package com.xrotune.music.core.network

/** Immutable snapshot of the current session used to build request params. */
data class SessionState(
    val uid: String = "",
    val token: String = "",
    val lang: String = "en",
    val country: String = "IN",
    val channelId: String = "1000",
    val verCode: String = "3100",
    val verName: String = "3.1.0",
    val isLoggedIn: Boolean = false,
) {
    val isGuest: Boolean get() = !isLoggedIn
}

interface SessionProvider {
    fun current(): SessionState

    /** Params appended (and signed) on every request. */
    fun commonParams(): Map<String, String> {
        val s = current()
        return linkedMapOf(
            "lang" to s.lang,
            "country" to s.country,
            "ver_code" to s.verCode,
            "ver_name" to s.verName,
            "os_type" to "1",
            "ui_mode" to "1",
            "channel_id" to s.channelId,
        )
    }
}
