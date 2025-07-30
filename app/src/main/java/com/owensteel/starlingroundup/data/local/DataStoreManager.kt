package com.owensteel.starlingroundup.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/*

    DataStore singleton

 */

class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    suspend fun <T> read(key: Preferences.Key<T>): T? {
        return dataStore.data.first()[key]
    }

    suspend fun remove(key: Preferences.Key<*>) {
        dataStore.edit { prefs -> prefs.remove(key) }
    }

}