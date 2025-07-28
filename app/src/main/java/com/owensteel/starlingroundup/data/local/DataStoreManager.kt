package com.owensteel.starlingroundup.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.owensteel.starlingroundup.util.SharedConstants.DATASTORE_NAME
import kotlinx.coroutines.flow.first

/*

    DataStore singleton

 */

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class DataStoreManager(private val context: Context) {

    suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }

    suspend fun <T> read(key: Preferences.Key<T>): T? {
        return context.dataStore.data.first()[key]
    }

    suspend fun remove(key: Preferences.Key<*>) {
        context.dataStore.edit { prefs -> prefs.remove(key) }
    }

}