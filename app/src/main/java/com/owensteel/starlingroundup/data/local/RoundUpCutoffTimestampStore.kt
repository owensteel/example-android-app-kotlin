package com.owensteel.starlingroundup.data.local

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.LATEST_ROUNDUP_CUTOFF_TIMESTAMP
import javax.inject.Inject

/*

    Gets and sets the round-up cutoff timestamp

 */

class RoundUpCutoffTimestampStore @Inject constructor(
    private val context: Context,
    private val dataStoreManager: DataStoreManager = DataStoreManager(context)
) {

    suspend fun saveLatestRoundUpCutoffTimestamp(cutoffTimestamp: String) {
        dataStoreManager.write(
            stringPreferencesKey(LATEST_ROUNDUP_CUTOFF_TIMESTAMP),
            cutoffTimestamp
        )
    }

    suspend fun getLatestRoundUpCutOffTimestamp(): String? {
        return dataStoreManager.read(stringPreferencesKey(LATEST_ROUNDUP_CUTOFF_TIMESTAMP))
    }

}