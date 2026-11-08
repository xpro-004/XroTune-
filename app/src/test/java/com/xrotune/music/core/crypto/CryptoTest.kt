package com.xrotune.music.core.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies request signing, secret obfuscation and stream XOR logic against the
 * vectors published for the backend.
 */
class CryptoTest {

    @Test
    fun `base64 matches known vectors`() {
        assertEquals("", B64.encode("".toByteArray()))
        assertEquals("Zg==", B64.encode("f".toByteArray()))
        assertEquals("Zm8=", B64.encode("fo".toByteArray()))
        assertEquals("Zm9v", B64.encode("foo".toByteArray()))
        assertEquals("Zm9vYmFy", B64.encode("foobar".toByteArray()))
        assertTrue("foobar".toByteArray().contentEquals(B64.decode("Zm9vYmFy")))
        assertTrue("f".toByteArray().contentEquals(B64.decode("Zg==")))
    }

    @Test
    fun `obfuscation round trips`() {
        listOf(
            "&PM9GikcERfy2yi6f",
            "rz18efAXUbdiaO7k",
            "VidMateDayDay_up",
            "Vmate_GOgoGOgoGO",
            "http://api.apk.v-mate.mobi/",
        ).forEach { plain ->
            assertEquals(plain, Secrets.deobf(Secrets.obf(plain)))
        }
    }

    @Test
    fun `secrets decode to the published values`() {
        assertEquals("&PM9GikcERfy2yi6f", Secrets.signSalt)
        assertEquals("rz18efAXUbdiaO7k", String(Secrets.aesKey, Charsets.ISO_8859_1))
        assertEquals("VidMateDayDay_up", String(Secrets.xorKey1, Charsets.ISO_8859_1))
        assertEquals("Vmate_GOgoGOgoGO", String(Secrets.xorKey2, Charsets.ISO_8859_1))
        assertEquals("http://api.apk.v-mate.mobi/", Secrets.baseHost)
    }

    @Test
    fun `sign is md5 of sorted params plus salt`() {
        val expected = Md5.hex("a=1&b=2&PM9GikcERfy2yi6f")
        assertEquals(expected, Signer.sign(mapOf("b" to "2", "a" to "1")))
    }

    @Test
    fun `sign ignores any existing sign field`() {
        val withoutSign = Signer.sign(mapOf("a" to "1", "b" to "2"))
        val withSign = Signer.sign(mapOf("a" to "1", "b" to "2", "sign" to "deadbeef"))
        assertEquals(withoutSign, withSign)
    }

    @Test
    fun `xor is symmetric and key is 16 bytes`() {
        val key = Secrets.xorKeyForType(1234)
        assertEquals(16, key.size)
        val data = "the quick brown fox jumps".toByteArray()
        val once = XorCrypto.decrypt(data, key)
        assertTrue(data.contentEquals(XorCrypto.decrypt(once, key)))
    }

    @Test
    fun `stream type selects the right key`() {
        assertTrue(Secrets.xorKeyForType(1234).contentEquals(Secrets.xorKey1))
        assertTrue(Secrets.xorKeyForType(1235).contentEquals(Secrets.xorKey2))
        assertTrue(Secrets.xorKeyForType(1236).contentEquals(Secrets.xorKey2))
    }
}
