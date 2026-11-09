package com.xrotune.music.data.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Routes playback through [XorDataSource] when the media item uses the
 * `xor://<type>/<encoded-url>` scheme, otherwise falls back to plain HTTP.
 *
 * This lets a single ExoPlayer instance mix encrypted native streams and
 * ordinary (already downloaded / plain) files.
 */
@OptIn(UnstableApi::class)
class XorAwareDataSourceFactory(context: Context) : DataSource.Factory {

    private val upstreamFactory = DefaultDataSource.Factory(context)

    override fun createDataSource(): DataSource = XorAwareDataSource(upstreamFactory.createDataSource())

    companion object {
        const val SCHEME = "xor"

        fun buildUri(realUrl: String, type: Int): Uri =
            Uri.parse("$SCHEME://$type/${URLEncoder.encode(realUrl, "UTF-8")}")
    }
}

@OptIn(UnstableApi::class)
private class XorAwareDataSource(private val upstream: DataSource) : DataSource {

    private var delegate: DataSource = upstream

    override fun open(dataSpec: DataSpec): Long {
        val uri = dataSpec.uri
        return if (uri.scheme.equals(XorAwareDataSourceFactory.SCHEME, ignoreCase = true)) {
            val type = uri.host?.toIntOrNull() ?: 1234
            val realUrl = URLDecoder.decode(uri.path.orEmpty().removePrefix("/"), "UTF-8")
            val realSpec = dataSpec.buildUpon().setUri(realUrl).build()
            delegate = XorDataSource(upstream, type)
            delegate.open(realSpec)
        } else {
            delegate = upstream
            upstream.open(dataSpec)
        }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int = delegate.read(buffer, offset, length)

    override fun getUri(): Uri? = delegate.uri

    override fun close() = delegate.close()

    override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) {
        upstream.addTransferListener(transferListener)
    }
}
