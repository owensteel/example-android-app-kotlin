package com.owensteel.starlingroundup.util

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals

class MoneyUtilsTest {

    private val moneyUtils = MoneyUtils

    @Test
    fun `Money roundUp util rounds up correctly`() = runTest {
        val roundUpResult: Long = moneyUtils.roundUp(123)
        assertEquals(roundUpResult, 77)
    }

}