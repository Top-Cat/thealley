package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.datetime.Instant

data class BrightReading(
    val time: Instant,
    val consumption: Float?
) {
    val m3 = consumption?.let { it * CONVERSION_FACTOR }

    companion object {
        private const val VOLUME_CORRECTION = 1.02264
        private const val CALORIFIC_VALUE = 39
        const val CONVERSION_FACTOR = 3.6 / CALORIFIC_VALUE / VOLUME_CORRECTION
    }
}
