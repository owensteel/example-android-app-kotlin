package com.owensteel.starlingroundup.data.local

import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ACCESS_TOKEN_EXPIRY
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ACCESS_TOKEN_IV
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ENCRYPTED_ACCESS_TOKEN
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ENCRYPTED_REFRESH_TOKEN
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.REFRESH_TOKEN_IV
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SecureTokenStoreTest {

    private val crypto: CryptoManager = mock()
    private val dataStore: DataStoreManager = mock()

    private lateinit var secureTokenStore: SecureTokenStore

    private val encryptedBytes = "encrypted".toByteArray()
    private val ivBytes = "iv".toByteArray()

    @Before
    fun setup() {
        // Override crypto with our mock for this test
        secureTokenStore = SecureTokenStore(dataStore, crypto)
        secureTokenStore.javaClass.getDeclaredField("crypto").apply {
            isAccessible = true
            set(secureTokenStore, crypto)
        }
    }

    @Test
    fun `saveAccessToken stores encrypted access token and expiry`() = runTest {
        whenever(crypto.encrypt("my-token")).thenReturn(ivBytes to encryptedBytes)

        secureTokenStore.saveAccessToken("my-token", 3600)

        verify(dataStore).write(byteArrayPreferencesKey(ENCRYPTED_ACCESS_TOKEN), encryptedBytes)
        verify(dataStore).write(byteArrayPreferencesKey(ACCESS_TOKEN_IV), ivBytes)
        verify(dataStore).write(
            eq(longPreferencesKey(ACCESS_TOKEN_EXPIRY)),
            argThat { this > System.currentTimeMillis() }
        )
    }

    @Test
    fun `getAccessToken returns decrypted token`() = runTest {
        whenever(dataStore.read(byteArrayPreferencesKey(ENCRYPTED_ACCESS_TOKEN)))
            .thenReturn(encryptedBytes)
        whenever(dataStore.read(byteArrayPreferencesKey(ACCESS_TOKEN_IV)))
            .thenReturn(ivBytes)
        whenever(crypto.decrypt(ivBytes, encryptedBytes)).thenReturn("decrypted-token")

        val result = secureTokenStore.getAccessToken()

        assertEquals("decrypted-token", result)
    }

    @Test
    fun `getAccessToken returns null if data is missing`() = runTest {
        whenever(dataStore.read(byteArrayPreferencesKey(ENCRYPTED_ACCESS_TOKEN)))
            .thenReturn(null)
        whenever(dataStore.read(byteArrayPreferencesKey(ACCESS_TOKEN_IV)))
            .thenReturn(null)

        val result = secureTokenStore.getAccessToken()

        assertNull(result)
        verify(crypto, never()).decrypt(any(), any())
    }

    @Test
    fun `saveRefreshToken stores encrypted refresh token`() = runTest {
        whenever(crypto.encrypt("refresh-token")).thenReturn(ivBytes to encryptedBytes)

        secureTokenStore.saveRefreshToken("refresh-token")

        verify(dataStore).write(byteArrayPreferencesKey(ENCRYPTED_REFRESH_TOKEN), encryptedBytes)
        verify(dataStore).write(byteArrayPreferencesKey(REFRESH_TOKEN_IV), ivBytes)
    }

    @Test
    fun `getRefreshToken returns decrypted refresh token`() = runTest {
        whenever(dataStore.read(byteArrayPreferencesKey(ENCRYPTED_REFRESH_TOKEN)))
            .thenReturn(encryptedBytes)
        whenever(dataStore.read(byteArrayPreferencesKey(REFRESH_TOKEN_IV)))
            .thenReturn(ivBytes)
        whenever(crypto.decrypt(ivBytes, encryptedBytes)).thenReturn("refresh-decrypted")

        val result = secureTokenStore.getRefreshToken()

        assertEquals("refresh-decrypted", result)
    }

    @Test
    fun `deleteSavedRefreshToken removes encrypted refresh token`() = runTest {
        secureTokenStore.deleteSavedRefreshToken()

        verify(dataStore).remove(byteArrayPreferencesKey(ENCRYPTED_REFRESH_TOKEN))
    }

    @Test
    fun `getAccessTokenExpiryTime returns expiry`() = runTest {
        val expectedExpiry = System.currentTimeMillis() + 5000
        whenever(dataStore.read(longPreferencesKey(ACCESS_TOKEN_EXPIRY))).thenReturn(expectedExpiry)

        val result = secureTokenStore.getAccessTokenExpiryTime()

        assertEquals(expectedExpiry, result)
    }
}
