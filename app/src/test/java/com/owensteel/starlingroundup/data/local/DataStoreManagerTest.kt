package com.owensteel.starlingroundup.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class DataStoreManagerTest {

    private lateinit var fakeDataStore: DataStore<Preferences>
    private lateinit var manager: DataStoreManager
    private lateinit var tempDir: File

    private val testKeyString = stringPreferencesKey("test_key_string")
    private val testKeyInt = intPreferencesKey("test_key_int")

    @Before
    fun setUp() {
        tempDir = createTempDir("datastore-unit")
        fakeDataStore = PreferenceDataStoreFactory.create {
            File(tempDir, "test.preferences_pb")
        }
        manager = DataStoreManager(fakeDataStore)

        // Hacky way of making the DataStoreManager use
        // the fake DataStore without confusing Hilt
        val field = manager.javaClass.getDeclaredField("dataStore")
        field.isAccessible = true
        field.set(manager, fakeDataStore)
    }

    @Test
    fun `write stores the correct value`() = runTest {
        manager.write(testKeyString, "Hello")
        val result = manager.read(testKeyString)
        assertEquals("Hello", result)
    }

    @Test
    fun `read returns null when key is missing`() = runTest {
        val result = manager.read(testKeyString)
        assertNull(result)
    }

    @Test
    fun `remove deletes a stored key`() = runTest {
        manager.write(testKeyInt, 42)
        assertEquals(42, manager.read(testKeyInt))

        manager.remove(testKeyInt)
        assertNull(manager.read(testKeyInt))
    }
}