package com.xrotune.music.data.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.xrotune.music.core.crypto.Secrets
import com.xrotune.music.core.crypto.XorCrypto
import java.io.IOException

/**
 * Wraps an upstream [DataSource] and de-scrambles the bytes on the fly with the
 * repeating 16-byte XOR key. ExoPlayer can seek inside the stream, so we track
 * the absolute byte offset and derive the key index from it.
 */
@OptIn(UnstableApi::class)
class XorDataSource(
    private val upstream: DataSource,
    private val type: Int,
) : BaseDataSource(/* isNetwork = */ true) {

    private val key: ByteArray = Secrets.xorKeyForType(type)
    private var position: Long = 0
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        transferInitializing(dataSpec)
        position = dataSpec.position
        val length = upstream.open(dataSpec)
        opened = true
        transferStarted(dataSpec)
        return length
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val read = upstream.read(buffer, offset, length)
        if (read <= 0) return read
        for (i in 0 until read) {
            val keyIndex = ((position + i) % key.size).toInt()
            buffer[offset + i] = (buffer[offset + i].toInt() xor key[keyIndex].toInt()).toByte()
        }
        position += read
        bytesTransferred(read)
        return read
    }

    override fun getUri(): Uri? = upstream.uri

    override fun close() {
        try {
            upstream.close()
        } finally {
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }

    @OptIn(UnstableApi::class)
    class Factory(private val upstreamFactory: DataSource.Factory, private val type: Int) : DataSource.Factory {
        override fun createDataSource(): DataSource = XorDataSource(upstreamFactory.createDataSource(), type)

        @Suppress("UNUSED_PARAMETER")
        fun setTransferListener(listener: TransferListener?) { /* no-op */ }
    }
}
