package com.owensteel.starlingroundup.model

import java.util.Currency
import java.util.Locale

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
        val pence = minorUnits % 100

        return String.format(Locale.UK, "%s%d.%02d", unitSymbol, pounds, pence)
    }

    // Round up to next pound (in minor units)
    fun roundUp(): Long {
        // Is the transaction amount a full pound?
        val remainder = minorUnits % 100
        // If not a full pound, return the difference
        // between it and a full pound
        return if (remainder == 0L) 0L else (100 - remainder)
    }
}