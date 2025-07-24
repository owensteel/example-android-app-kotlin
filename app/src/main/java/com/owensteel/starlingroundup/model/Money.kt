package com.owensteel.starlingroundup.model

import java.util.Currency
import java.util.Locale
import kotlin.math.abs

/*

    A centralised formatter for money sums, when
    provided in minor units and with a specified
    currency

 */

data class Money(
    val currency: String,
    val minorUnits: Long
) {
    // Formatting
    override fun toString(): String {
        val unitSymbol = try {
            Currency.getInstance(currency)
                .getSymbol(Locale.UK) // locale just determines how symbols are rendered
        } catch (e: Exception) {
            currency
        }

        val pounds = minorUnits / 100
        val pence = abs(minorUnits % 100) // Prevents negative pence (impossible)

        return String.format(Locale.UK, "%s%d.%02d", unitSymbol, pounds, pence)
    }
}