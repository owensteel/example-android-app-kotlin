package com.owensteel.starlingroundup.util

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.owensteel.starlingroundup.R
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime

object DateTimeUtils {
    // Get time since string, for a more user-friendly date/time expression
    @Composable
    fun timeSince(isoString: String): String {
        val context = LocalContext.current
        val resources = context.resources
        return timeSince(resources, isoString)
    }

    fun timeSince(context: Context, isoString: String): String {
        val resources = context.resources
        return timeSince(resources, isoString)
    }

    fun timeSince(resources: Resources, isoString: String): String {
        val now = ZonedDateTime.now()
        val then = Instant.parse(isoString).atZone(ZoneId.systemDefault())

        val period = Period.between(then.toLocalDate(), now.toLocalDate())
        val duration = Duration.between(then, now)

        return when {
            period.years > 0 -> resources.getQuantityString(
                R.plurals.time_ago_years,
                period.years,
                period.years
            )

            period.months > 0 -> resources.getQuantityString(
                R.plurals.time_ago_months,
                period.months,
                period.months
            )

            period.days > 0 -> resources.getQuantityString(
                R.plurals.time_ago_days,
                period.days,
                period.days
            )

            duration.toHours() > 0 -> {
                val hours = duration.toHours().toInt()
                resources.getQuantityString(
                    R.plurals.time_ago_hours,
                    hours,
                    hours
                )
            }

            duration.toMinutes() > 0 -> {
                val minutes = duration.toMinutes().toInt()
                resources.getQuantityString(
                    R.plurals.time_ago_minutes,
                    minutes,
                    minutes
                )
            }

            duration.seconds > 0 -> {
                val seconds = duration.seconds.toInt()
                resources.getQuantityString(
                    R.plurals.time_ago_seconds,
                    seconds,
                    seconds
                )
            }

            else -> resources.getString(R.string.date_time_since_just_now)
        }
    }

    // Get day of month, with suffix
    @Composable
    fun getDayWithSuffix(day: Int): String {
        if (day !in 1..31) throw IllegalArgumentException("Day must be between 1 and 31")

        val suffix = when {
            day in 11..13 -> stringResource(R.string.day_suffix_th)
            day % 10 == 1 -> stringResource(R.string.day_suffix_st)
            day % 10 == 2 -> stringResource(R.string.day_suffix_nd)
            day % 10 == 3 -> stringResource(R.string.day_suffix_rd)
            else -> stringResource(R.string.day_suffix_th)
        }

        return stringResource(R.string.day_with_suffix, day, suffix)
    }
}