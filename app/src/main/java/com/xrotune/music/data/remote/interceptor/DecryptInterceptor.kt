package com.xrotune.music.data.remote.interceptor

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.xrotune.music.core.crypto.AesDecryptor
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Transparently decrypts the envelope returned by the backend:
 *
 * { "status": 1, "msg": "", "data": "<base64 AES/CBC/NoPadding>" }
 *
 * The decrypted payload is parsed as JSON when possible and re-injected as the
 * new "data" value so callers only ever see plain JSON.
 */
@Singleton
class DecryptInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val contentType = response.body?.contentType()
        if (contentType == null || !contentType.subtype.contains("json", ignoreCase = true)) {
            return response
        }
        val bodyString = try {
            response.body!!.string()
        } catch (t: Throwable) {
            return response
        }
        if (bodyString.isBlank() || !bodyString.trimStart().startsWith("{")) return response

        val rewritten = try {
            rewrite(bodyString)
        } catch (t: Throwable) {
            bodyString
        }
        return response.newBuilder()
            .body(rewritten.toResponseBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    private fun rewrite(body: String): String {
        val root = JsonParser.parseString(body)
        if (!root.isJsonObject) return body
        val obj = root.asJsonObject
        val data = obj.get("data")
        if (data == null || !data.isJsonPrimitive) return body

        val raw = data.asString
        val decrypted = try {
            AesDecryptor.decrypt(raw)
        } catch (t: Throwable) {
            return body
        }
        if (decrypted.isBlank()) return body

        val element: JsonElement = try {
            JsonParser.parseString(decrypted)
        } catch (t: Throwable) {
            JsonParser.parseString("null")
        }
        obj.add("data", element)
        return obj.toString()
    }
}
