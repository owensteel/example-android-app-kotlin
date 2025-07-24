package com.owensteel.starlingroundup.util

object MoneyUtils {
    // Round up to next pound (in minor units)
    fun roundUp(minorUnits: Long): Long {
        // Is the transaction amount a full pound?
        val remainder = minorUnits % 100
        // If not a full pound, return the difference
        // between it and a full pound
        return if (remainder == 0L) 0L else (100 - remainder)
    }
}