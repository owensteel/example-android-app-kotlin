package com.owensteel.starlingroundup.insttestutils

fun waitUntil(timeoutMs: Long = 2000, intervalMs: Long = 50, condition: () -> Boolean): Boolean {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
        if (condition()) return true
        Thread.sleep(intervalMs)
    }
    return false
}
