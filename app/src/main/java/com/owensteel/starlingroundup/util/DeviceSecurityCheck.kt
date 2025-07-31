package com.owensteel.starlingroundup.util

import android.content.Context
import android.provider.Settings.Global.ADB_ENABLED
import com.owensteel.starlingroundup.BuildConfig
import com.scottyab.rootbeer.RootBeer
import java.io.File
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/*

    Device security, device vulnerability detection utility

    Our network security considerations, e.g certificate
    pinning, would be useless if an attacker has rooted
    the app or is using Frida, as that toolkit can simply
    bypass verification by hooking in at runtime or replacing
    the pinned certificate
    Here we attempt to detect any common signs of Frida (or
    similar frameworks like Xposed) being installed, and signs
    of rooting or inspection

*/

interface IDeviceSecurityCheck {
    fun isCompromised(context: Context): Boolean
}

@Singleton
class DeviceSecurityCheck @Inject constructor() : IDeviceSecurityCheck {

    // Check for rooting with RootBeer library
    private fun isRooted(context: Context): Boolean {
        val rootBeer = RootBeer(context)
        return rootBeer.isRooted
    }

    // Check for common Frida server pipes and binaries
    private fun isFridaDetected(): Boolean {
        val fridaPaths = listOf(
            "/data/local/tmp/frida-server",
            "/data/local/frida-server",
            "/data/local/tmp/re.frida.server",
            "/data/local/tmp/fs0",
            "/data/local/tmp/frida",
            "/dev/com.frida.piped"
        )

        // Basic check if there is a file at any path
        return fridaPaths.any { File(it).exists() }
    }

    internal var adbEnabledHook: (Context) -> Boolean = { context ->
        try {
            android.provider.Settings.Secure.getInt(
                context.contentResolver,
                ADB_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    // USB debugging can be a sign of attempted hooking
    // or inspecting
    private fun isUsbDebuggingEnabled(context: Context): Boolean {
        // Note: will always return positive if app is
        // being run on an emulator, due to the nature
        // of the simulated device
        return adbEnabledHook(context)
    }

    // Runtime check for debugger
    private fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }

    // Testable hook for hasSuspiciousClasses()
    internal var classLoaderHook: (String) -> Class<*> = { name -> Class.forName(name) }

    // Detect class injection via reflection
    private fun hasSuspiciousClasses(): Boolean {
        val suspiciousClassNames = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XC_MethodHook",
            "frida"
        )

        return suspiciousClassNames.any {
            try {
                classLoaderHook(it)
                true // Found suspicious class
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    // Frida typically uses ports like 27042 or 27043
    // Try binding or probing these ports
    private fun isFridaPortOpen(): Boolean {
        val fridaTypicalPorts = listOf(
            27042,
            27043
        )

        return fridaTypicalPorts.any {
            try {
                Socket("127.0.0.1", it).use {
                    true // Frida port in use
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    // Detect known root properties
    // Root toolkits can expose system properties
    // that may be queried
    private fun hasRootProps(): Boolean {
        val props = listOf(
            "ro.debuggable",
            "ro.secure",
            "ro.build.tags"
        )

        return props.any { prop ->
            // Get value returned by executing "get" on
            // this property
            val value = try {
                Runtime.getRuntime().exec("get prop $prop")
                    .inputStream.bufferedReader().readLine()
            } catch (e: Exception) {
                null
            }

            // Each property has a suspicious value that
            // getting may return if executed in a rooted
            // environment
            // This switch statement maps each checked property
            // to its suspicious value
            when (prop) {
                "ro.build.tags" -> value?.contains("test-keys") == true
                "ro.debuggable" -> value == "1"
                "ro.secure" -> value == "0"
                else -> false
            }
        }
    }

    // Main
    override fun isCompromised(context: Context): Boolean {
        return isRooted(context) ||
                isFridaDetected() ||
                // USB debugging is always "on" during debugging
                // So prevent a false positive
                (!BuildConfig.DEBUG && isUsbDebuggingEnabled(context)) ||
                // Runtime debugger check as back-up
                isDebuggerAttached() ||
                hasSuspiciousClasses() ||
                isFridaPortOpen() ||
                hasRootProps()
    }
}