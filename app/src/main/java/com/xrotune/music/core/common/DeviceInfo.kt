package com.xrotune.music.core.common

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Stable, spoofed device identity sent with every API call. */
@Singleton
class DeviceInfo @Inject constructor(@ApplicationContext private val context: Context) {

    val androidId: String by lazy {
        runCatching {
            android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "xrotune00000000"
    }

    val osVersion: String = Build.VERSION.RELEASE ?: "13"

    val model: String = Build.MODEL ?: "Android"

    val brand: String = Build.BRAND ?: "Android"

    /** A stable pseudo IMEI so the server treats us as one device. */
    val imei: String by lazy { "86" + androidId.filter { it.isLetterOrDigit() }.padEnd(13, '0').take(13) }

    val ua: String = "Mozilla/5.0 (Linux; Android $osVersion; $model) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
}
