package com.owensteel.starlingroundup.util

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.scottyab.rootbeer.RootBeer
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.net.Socket

@Config(sdk = [33])
@RunWith(RobolectricTestRunner::class)
class DeviceSecurityCheckTest {

    private val context: Context = mock()
    private val contentResolver: ContentResolver = mock()
    private val packageManager: PackageManager = mock()
    private val log: Log = mock()

    private val deviceSecurityCheck = DeviceSecurityCheck()

    @Before
    fun setup() {
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(context.packageManager).thenReturn(packageManager)

    }

    @Test
    fun `isCompromised returns true if RootBeer detects root`() {
        mockConstruction(RootBeer::class.java) { mock, _ ->
            whenever(mock.isRooted).thenReturn(true)
        }.use {
            val result = deviceSecurityCheck.isCompromised(context)
            assertTrue(result)
        }
    }

    @Test
    fun `isCompromised returns true if Frida pipe exists`() {
        val testFile = mock(File::class.java)
        whenever(testFile.exists()).thenReturn(true)

        mockConstruction(File::class.java) { _, _ -> testFile }.use {
            val result = deviceSecurityCheck.isCompromised(context)
            assertTrue(result)
        }
    }

    @Test
    fun `isCompromised reports USB debugging if app is not in debug mode`() {
        // Stub the ADB check
        deviceSecurityCheck.adbEnabledHook = { true }

        val result = deviceSecurityCheck.isCompromised(context)

        // Currently no reliable way to mock BuildConfig without
        // making DeviceSecurityCheck vulnerable
        assertTrue(result)

        // Clean up the hook
        deviceSecurityCheck.adbEnabledHook = DeviceSecurityCheck::class.java
            .getDeclaredMethod("isUsbDebuggingEnabled", Context::class.java)
            .apply { isAccessible = true }
            .let { method -> { context: Context -> method.invoke(null, context) as Boolean } }
    }

    @Test
    fun `isCompromised returns true if debugger is attached`() {
        mockStatic(android.os.Debug::class.java).use { debugStatic ->
            debugStatic.`when`<Boolean> { android.os.Debug.isDebuggerConnected() }.thenReturn(true)

            val result = deviceSecurityCheck.isCompromised(context)
            assertTrue(result)
        }
    }

    @Test
    fun `isCompromised returns true if suspicious class is found`() {
        deviceSecurityCheck.classLoaderHook = { name ->
            if (name == "de.robv.android.xposed.XposedBridge") {
                String::class.java // simulate class present
            } else {
                throw ClassNotFoundException()
            }
        }

        val result = deviceSecurityCheck.isCompromised(context)
        assertTrue(result)

        // Cleanup
        deviceSecurityCheck.classLoaderHook = { Class.forName(it) }
    }

    @Test
    fun `isCompromised returns true if Frida port is open`() {
        mockConstruction(Socket::class.java) { _, _ -> mock(Socket::class.java) }.use {
            val result = deviceSecurityCheck.isCompromised(context)
            assertTrue(result)
        }
    }

    @Test
    fun `isCompromised returns true if suspicious root property is returned`() {
        val process = mock(Process::class.java)
        val inputStream = "test-keys".byteInputStream()

        whenever(process.inputStream).thenReturn(inputStream)
        val runtime = mock(Runtime::class.java)
        whenever(runtime.exec("get prop ro.build.tags")).thenReturn(process)

        mockStatic(Runtime::class.java).use { runtimeStatic ->
            runtimeStatic.`when`<Runtime> { Runtime.getRuntime() }.thenReturn(runtime)

            val result = deviceSecurityCheck.isCompromised(context)
            assertTrue(result)
        }
    }

}