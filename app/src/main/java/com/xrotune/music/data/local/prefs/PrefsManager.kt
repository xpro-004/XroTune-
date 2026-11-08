package com.xrotune.music.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xrotune.music.core.network.SessionProvider
import com.xrotune.music.core.network.SessionState
import com.xrotune.music.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xrotune_prefs")

/**
 * Single source of truth for user preferences and the auth session.
 * Acts as a [SessionProvider] so the network layer always has fresh credentials.
 */
@Singleton
class PrefsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SessionProvider {

    private object Keys {
        val UID = stringPreferencesKey("uid")
        val TOKEN = stringPreferencesKey("token")
        val PHONE = stringPreferencesKey("phone")
        val NICKNAME = stringPreferencesKey("nickname")
        val AVATAR = stringPreferencesKey("avatar")
        val LANG = stringPreferencesKey("lang")
        val COUNTRY = stringPreferencesKey("country")
        val WIFI_ONLY_DOWNLOAD = booleanPreferencesKey("wifi_only_download")
        val HIGH_QUALITY = booleanPreferencesKey("high_quality")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { !it[Keys.TOKEN].isNullOrBlank() }

    val userFlow: Flow<User?> = context.dataStore.data.map { prefs ->
        val token = prefs[Keys.TOKEN]
        if (token.isNullOrBlank()) null
        else User(
            uid = prefs[Keys.UID] ?: "",
            nickname = prefs[Keys.NICKNAME] ?: "XroTune User",
            avatarUrl = prefs[Keys.AVATAR],
            phone = prefs[Keys.PHONE],
            token = token,
        )
    }

    val wifiOnlyDownload: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIFI_ONLY_DOWNLOAD] ?: true }
    val highQuality: Flow<Boolean> = context.dataStore.data.map { it[Keys.HIGH_QUALITY] ?: true }
    val autoplay: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTOPLAY] ?: true }

    // ---- SessionProvider (called synchronously from the OkHttp thread) ----
    override fun current(): SessionState = runBlocking {
        val prefs = context.dataStore.data.first()
        SessionState(
            uid = prefs[Keys.UID] ?: "",
            token = prefs[Keys.TOKEN] ?: "",
            lang = prefs[Keys.LANG] ?: "en",
            country = prefs[Keys.COUNTRY] ?: "IN",
            isLoggedIn = !prefs[Keys.TOKEN].isNullOrBlank(),
        )
    }

    suspend fun saveSession(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UID] = user.uid
            prefs[Keys.TOKEN] = user.token
            prefs[Keys.PHONE] = user.phone ?: ""
            prefs[Keys.NICKNAME] = user.nickname
            prefs[Keys.AVATAR] = user.avatarUrl ?: ""
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.UID)
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.PHONE)
            prefs.remove(Keys.NICKNAME)
            prefs.remove(Keys.AVATAR)
        }
    }

    suspend fun setLanguage(lang: String) = context.dataStore.edit { it[Keys.LANG] = lang }

    suspend fun setCountry(country: String) = context.dataStore.edit { it[Keys.COUNTRY] = country }

    suspend fun setWifiOnlyDownload(enabled: Boolean) = context.dataStore.edit { it[Keys.WIFI_ONLY_DOWNLOAD] = enabled }

    suspend fun setHighQuality(enabled: Boolean) = context.dataStore.edit { it[Keys.HIGH_QUALITY] = enabled }

    suspend fun setAutoplay(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTOPLAY] = enabled }
}
