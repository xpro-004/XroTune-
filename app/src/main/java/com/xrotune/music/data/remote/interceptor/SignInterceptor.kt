package com.xrotune.music.data.remote.interceptor

import com.xrotune.music.core.crypto.Signer
import com.xrotune.music.core.network.SessionProvider
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Appends the common + auth params and the MD5 request signature to every call.
 *
 * sign = MD5(sorted("k=v" joined by '&') + "&PM9GikcERfy2yi6f")
 */
@Singleton
class SignInterceptor @Inject constructor(
    private val session: SessionProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val params = LinkedHashMap<String, String>()

        // existing query params
        request.url.queryParameterNames.forEach { name ->
            request.url.queryParameter(name)?.let { params[name] = it }
        }

        // form body params (if any)
        (request.body as? FormBody)?.let { form ->
            for (i in 0 until form.size) {
                params[form.name(i)] = form.value(i)
            }
        }

        params.putAll(session.commonParams())

        val state = session.current()
        if (state.isLoggedIn) {
            params["uid"] = state.uid
            params["token"] = state.token
        }

        val signedUrl = request.url.newBuilder().apply {
            // rebuild query from scratch so the signed set matches what is sent
            query(null)
            params.forEach { (k, v) -> addQueryParameter(k, v) }
            addQueryParameter("sign", Signer.sign(params))
        }.build()

        return chain.proceed(request.newBuilder().url(signedUrl).build())
    }
}
