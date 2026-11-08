package com.xrotune.music.core.crypto

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Standard RFC 4648 Base64. Hand-rolled so the crypto layer stays free of
 * Android framework classes (and therefore unit-testable on the JVM).
 */
internal object B64 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(input: ByteArray): String {
        val sb = StringBuilder((input.size + 2) / 3 * 4)
        var i = 0
        while (i < input.size) {
            val b0 = input[i].toInt() and 0xFF
            val b1 = if (i + 1 < input.size) input[i + 1].toInt() and 0xFF else -1
            val b2 = if (i + 2 < input.size) input[i + 2].toInt() and 0xFF else -1
            sb.append(ALPHABET[b0 shr 2])
            sb.append(ALPHABET[((b0 and 0x03) shl 4) or (if (b1 >= 0) b1 shr 4 else 0)])
            sb.append(if (b1 >= 0) ALPHABET[((b1 and 0x0F) shl 2) or (if (b2 >= 0) b2 shr 6 else 0)] else '=')
            sb.append(if (b2 >= 0) ALPHABET[b2 and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }

    fun decode(input: String): ByteArray {
        val clean = input.trim().filterNot { it.isWhitespace() }
        val out = java.io.ByteArrayOutputStream(clean.length / 4 * 3)
        var buffer = 0
        var bits = 0
        for (c in clean) {
            if (c == '=') break
            val v = ALPHABET.indexOf(c)
            if (v < 0) continue
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.write((buffer shr bits) and 0xFF)
            }
        }
        return out.toByteArray()
    }
}

/**
 * Secret material used across the app. Every constant is stored XOR-obfuscated
 * (key = "android.view") so that plain secrets never appear in the binary.
 */
object Secrets {

    private const val OBF_KEY = "android.view"

    // "&PM9GikcERfy2yi6f"
    private const val OBF_SIGN_SALT = "Rz4pSygAD00zOwMOUxcNRAk="

    // "rz18efAXUbdiaO7k"  (AES key == IV)
    private const val OBF_AES_KEY = "ExRVSgoPJXYjCwEeACFTGQ=="

    // "VidMateDayDay_up" (XOR key for stream type 1234)
    private const val OBF_XOR_KEY_1 = "NwcAPw4dAWoXECEWGDERAg=="

    // "Vmate_GOgoGOgoGO" (XOR key for stream types 1235 / 1236)
    private const val OBF_XOR_KEY_2 = "NwMFBgo2I2ERBiI4BgEjPQ=="

    // "http://api.apk.v-mate.mobi/"
    private const val OBF_BASE_HOST = "CRoQAlVGS08GAEsWEQVKBEIEBVoTRwgYAwdL"

    val signSalt: String get() = deobf(OBF_SIGN_SALT)
    val aesKey: ByteArray get() = deobf(OBF_AES_KEY).toByteArray(Charsets.ISO_8859_1)
    val xorKey1: ByteArray get() = deobf(OBF_XOR_KEY_1).toByteArray(Charsets.ISO_8859_1)
    val xorKey2: ByteArray get() = deobf(OBF_XOR_KEY_2).toByteArray(Charsets.ISO_8859_1)
    val baseHost: String get() = deobf(OBF_BASE_HOST)

    /** XOR key for a native media stream [type]. */
    fun xorKeyForType(type: Int): ByteArray = when (type) {
        1235, 1236 -> xorKey2
        else -> xorKey1
    }

    fun obf(plain: String): String {
        val k = OBF_KEY.toByteArray(Charsets.ISO_8859_1)
        val b = plain.toByteArray(Charsets.ISO_8859_1)
        return B64.encode(ByteArray(b.size) { i -> (b[i].toInt() xor k[i % k.size].toInt()).toByte() })
    }

    fun deobf(obfuscated: String): String {
        val k = OBF_KEY.toByteArray(Charsets.ISO_8859_1)
        val b = B64.decode(obfuscated)
        return String(ByteArray(b.size) { i -> (b[i].toInt() xor k[i % k.size].toInt()).toByte() }, Charsets.ISO_8859_1)
    }
}

object Md5 {
    fun hex(input: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(input).joinToString("") { "%02x".format(it) }

    fun hex(input: String): String = hex(input.toByteArray(Charsets.UTF_8))
}

/** MD5(paramString + "&PM9GikcERfy2yi6f") over sorted key=value pairs joined with '&'. */
object Signer {
    fun sign(params: Map<String, String>): String {
        val paramString = params.entries
            .filter { it.key != "sign" }
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
        return Md5.hex(paramString + Secrets.signSalt)
    }
}

/** AES/CBC/NoPadding with Key == IV == "rz18efAXUbdiaO7k". */
object AesDecryptor {
    fun decrypt(base64: String): String {
        val raw = B64.decode(base64)
        val key = Secrets.aesKey
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(key))
        val plain = cipher.doFinal(raw)
        return String(plain, Charsets.UTF_8).trimEnd('\u0000')
    }
}

/** Repeating 16-byte XOR used to de-scramble native media streams. */
object XorCrypto {
    fun decrypt(input: ByteArray, key: ByteArray): ByteArray {
        val out = ByteArray(input.size)
        for (i in input.indices) {
            out[i] = (input[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return out
    }
}
