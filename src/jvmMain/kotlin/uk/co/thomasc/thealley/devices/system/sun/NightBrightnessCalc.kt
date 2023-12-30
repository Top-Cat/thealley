package uk.co.thomasc.thealley.devices.system.sun

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min

object NightBrightnessCalc {
    fun getBrightnessFor(now: Instant): Int = getBrightnessFor(now.toLocalDateTime(TimeZone.UTC))
    private fun getBrightnessFor(now: LocalDateTime): Int {
        val totalMinutes = if (now.hour < 2 || now.hour > 12) {
            // Count down to 2am
            val hoursUntilTwoAM = if (now.hour > 2) 25 - now.hour else 1 - now.hour

            (hoursUntilTwoAM * 60) + (60 - now.minute)
        } else if (now.hour > 4) {
            // Count up from 5am

            ((now.hour - 5) * 60) + now.minute
        } else {
            0
        }

        val scaled = (min(totalMinutes, OVER_TIME) * (100 - MIN_PERCENT)) / OVER_TIME
        return MIN_PERCENT + scaled
    }

    private const val OVER_TIME = 120
    private const val MIN_PERCENT = 20
}
