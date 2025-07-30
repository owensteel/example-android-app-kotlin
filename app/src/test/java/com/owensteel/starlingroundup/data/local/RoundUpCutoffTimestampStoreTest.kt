package com.owensteel.starlingroundup.data.local

import androidx.datastore.preferences.core.stringPreferencesKey
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.LATEST_ROUNDUP_CUTOFF_TIMESTAMP
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RoundUpCutoffTimestampStoreTest {

    private val dataStoreManager: DataStoreManager = mock()
    private lateinit var store: RoundUpCutoffTimestampStore

    @Before
    fun setup() {
        store = RoundUpCutoffTimestampStore(dataStoreManager)
    }

    @Test
    fun `saveLatestRoundUpCutoffTimestamp saves correctly`() = runTest {
        store.saveLatestRoundUpCutoffTimestamp("2025-07-30T10:00:00Z")

        verify(dataStoreManager).write(
            stringPreferencesKey(LATEST_ROUNDUP_CUTOFF_TIMESTAMP),
            "2025-07-30T10:00:00Z"
        )
    }
}
