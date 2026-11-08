package com.xrotune.music.data.repository.impl

import com.xrotune.music.core.common.Result
import com.xrotune.music.core.common.runCatchingApp
import com.xrotune.music.data.local.prefs.PrefsManager
import com.xrotune.music.data.remote.ApiService
import com.xrotune.music.data.remote.dto.toDomain
import com.xrotune.music.domain.model.User
import com.xrotune.music.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val prefs: PrefsManager,
) : UserRepository {

    override val isLoggedIn: Flow<Boolean> = prefs.isLoggedInFlow
    override val user: Flow<User?> = prefs.userFlow

    override suspend fun sendCode(phone: String): Result<String> = withContext(Dispatchers.IO) {
        runCatchingApp {
            runCatching { api.prepareLogin(phone = phone) }
            val resp = api.sendSmsCode(phone = phone)
            val body = resp.data
            body?.token ?: ""
        }
    }

    override suspend fun verifyAndLogin(phone: String, code: String): Result<User> = withContext(Dispatchers.IO) {
        runCatchingApp {
            val response = runCatching { api.submitLogin(phone = phone, code = code) }
                .getOrElse { api.bind(phone = phone, code = code) }
            val dto = response.data ?: throw com.xrotune.music.core.common.AppError.Empty("Login failed")
            val user = dto.toDomain().copy(phone = phone)
            prefs.saveSession(user)
            user
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) { prefs.clearSession() }
}
