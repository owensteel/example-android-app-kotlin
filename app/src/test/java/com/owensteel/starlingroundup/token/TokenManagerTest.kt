package com.owensteel.starlingroundup.token

import com.owensteel.starlingroundup.BuildConfig
import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.model.TokenResponse
import com.owensteel.starlingroundup.network.StarlingAuthApi
import com.owensteel.starlingroundup.network.StarlingAuthApiProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

private const val FAKE_TOKEN_EXPIRY = 3600

class TokenManagerTest {

    private lateinit var tokenStore: SecureTokenStore
    private lateinit var mockAuthApiProvider: StarlingAuthApiProvider
    private lateinit var mockAuthApi: StarlingAuthApi
    private lateinit var tokenManager: TokenManager

    private val validToken = "valid-access-token"
    private val refreshToken = "refresh-token"

    @Before
    fun setup() = runBlocking {
        tokenStore = mock()

        mockAuthApi = mock()

        mockAuthApiProvider = mock()
        whenever(mockAuthApiProvider.getAuthApi()).thenReturn(mockAuthApi)

        tokenManager = TokenManager(tokenStore, mockAuthApiProvider)
    }

    @Test
    fun `getValidAccessTokenBlocking returns cached token when not expired`() = runTest {
        whenever(tokenStore.getAccessToken()).thenReturn(validToken)
        whenever(tokenStore.getAccessTokenExpiryTime()).thenReturn(System.currentTimeMillis() + 10000)

        val result = tokenManager.getValidAccessTokenBlocking()

        assertEquals(validToken, result)
        verify(tokenStore, never()).getRefreshToken()
    }

    @Test
    fun `getValidAccessTokenBlocking refreshes token when expired`() = runTest {
        val newToken = "refreshed-token"
        val tokenResponse = TokenResponse(
            newToken,
            "access_token",
            FAKE_TOKEN_EXPIRY,
            refreshToken
        )

        whenever(tokenStore.getAccessToken()).thenReturn(validToken)
        whenever(tokenStore.getAccessTokenExpiryTime()).thenReturn(System.currentTimeMillis() - 1000)
        whenever(tokenStore.getRefreshToken()).thenReturn(refreshToken)

        val successResponse = Response.success(tokenResponse)
        whenever(mockAuthApi.refreshAccessToken(any(), any(), any(), any())).thenReturn(
            successResponse
        )

        val result = tokenManager.getValidAccessTokenBlocking()

        assertEquals(newToken, result)
        verify(tokenStore).saveAccessToken(newToken, FAKE_TOKEN_EXPIRY)
    }

    @Test
    fun `refresh retries with fallback if refresh token is invalid`() = runTest {
        val initialResponse =
            Response.error<TokenResponse>(400, """{"error":"invalid_grant"}""".toResponseBody())
        val secondResponse = Response.success(
            TokenResponse(
                "fallback-token",
                "access_token",
                FAKE_TOKEN_EXPIRY,
                refreshToken
            )
        )

        whenever(tokenStore.getRefreshToken()).thenReturn("expired-refresh-token")
        whenever(mockAuthApi.refreshAccessToken(any(), any(), any(), any()))
            .thenReturn(initialResponse)
            .thenReturn(secondResponse)

        val result = tokenManager.invalidateAndRefreshCurrentAccessTokenBlocking()

        assertEquals("fallback-token", result)
        verify(tokenStore).deleteSavedRefreshToken()
    }

    @Test(expected = Exception::class)
    fun `refresh throws on non-retry-able failure`() = runTest {
        val errorResponse = Response.error<TokenResponse>(401, "Unauthorized".toResponseBody())

        whenever(tokenStore.getRefreshToken()).thenReturn(refreshToken)
        whenever(mockAuthApi.refreshAccessToken(any(), any(), any(), any())).thenReturn(
            errorResponse
        )

        tokenManager.invalidateAndRefreshCurrentAccessTokenBlocking()
    }

    @Test
    fun `refresh saves new refresh token if provided`() = runTest {
        val newRefreshToken = "new-refresh"

        val tokenResponse = TokenResponse(
            "new-access",
            "access_token",
            FAKE_TOKEN_EXPIRY,
            newRefreshToken
        )

        whenever(tokenStore.getRefreshToken()).thenReturn(refreshToken)

        val successResponse = Response.success(tokenResponse)
        whenever(mockAuthApi.refreshAccessToken(any(), any(), any(), any())).thenReturn(
            successResponse
        )

        val result = tokenManager.invalidateAndRefreshCurrentAccessTokenBlocking()

        assertEquals("new-access", result)
        verify(tokenStore).saveAccessToken("new-access", FAKE_TOKEN_EXPIRY)
        verify(tokenStore).saveRefreshToken(newRefreshToken)
    }

    @Test
    fun `refresh uses initial refresh token if secure token store returns null`() = runTest {
        val refreshFallback = BuildConfig.INIT_REFRESH_TOKEN

        // Return a null refresh token from the store
        whenever(tokenStore.getRefreshToken()).thenReturn(null)

        // For verifying that the "new access token" is received
        val newAccessToken = "new-access"
        val successResponse = Response.success(
            TokenResponse(
                newAccessToken,
                "access_token",
                FAKE_TOKEN_EXPIRY,
                "new-refresh"
            )
        )
        whenever(mockAuthApi.refreshAccessToken(any(), any(), any(), any())).thenReturn(
            successResponse
        )
        val result = tokenManager.invalidateAndRefreshCurrentAccessTokenBlocking()

        // Check we actually call Auth API with fallback refresh token
        verify(mockAuthApi).refreshAccessToken(
            any(),
            eq(refreshFallback),
            any(),
            any()
        )
        // Verify new access token is received
        assertEquals(result, newAccessToken)
    }
}