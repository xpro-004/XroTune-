package com.xrotune.music.core.common

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: AppError) : Result<Nothing>

    fun getOrNull(): T? = (this as? Success)?.data
}

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable? = null) : AppError("No internet connection", cause)
    class Timeout(cause: Throwable? = null) : AppError("Request timed out", cause)
    class Server(val status: Int, msg: String = "Server error ($status)") : AppError(msg)
    class Decryption(cause: Throwable? = null) : AppError("Failed to read server response", cause)
    class Empty(msg: String = "No results found") : AppError(msg)
    class Unknown(cause: Throwable? = null) : AppError(cause?.message ?: "Something went wrong", cause)
}

inline fun <T> runCatchingApp(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (t: Throwable) {
    Result.Failure(t.toAppError())
}

fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is java.net.UnknownHostException -> AppError.Network(this)
    is java.net.ConnectException -> AppError.Network(this)
    is java.net.SocketTimeoutException -> AppError.Timeout(this)
    is java.io.IOException -> AppError.Network(this)
    else -> AppError.Unknown(this)
}
