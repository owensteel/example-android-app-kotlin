package com.owensteel.starlingroundup.util

object StringUtils {

    fun pluralize(count: Long, unit: String): String {
        return "$count $unit${if (count != 1L) "s" else ""}"
    }

}