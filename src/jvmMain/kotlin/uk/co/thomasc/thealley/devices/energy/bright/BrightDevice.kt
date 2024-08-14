package uk.co.thomasc.thealley.devices.energy.bright

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.energy.bright.client.Bright
import uk.co.thomasc.thealley.devices.energy.bright.client.BrightPeriod
import uk.co.thomasc.thealley.devices.energy.bright.client.BrightResourceType
import uk.co.thomasc.thealley.devices.state.energy.bright.BrightState
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.types.BrightConfig
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

class BrightDevice(id: Int, config: BrightConfig, state: BrightState, stateStore: IStateUpdater<BrightState>) :
    AlleyDevice<BrightDevice, BrightConfig, BrightState>(id, config, state, stateStore) {

    private val bright = Bright(config.email, config.pass)
    private fun nextHalfHour(instant: Instant) = ((instant.epochSeconds / 1800) + 1) * 1800

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<TickEvent> {
            val now = Clock.System.now()
            if (state.nextCatchup?.let { now > it } != false) {
                val catchup = bright.catchup(BrightResourceType.GAS_CONSUMPTION)
                if (catchup.data.status != null) {
                    logger.info { "Bright catchup initiated ${catchup.data.status}" }
                }

                val from = state.latestReading.plus(1.hours)

                // Download 2 days of readings
                val readings = if (from < now) {
                    logger.info { "Getting readings from '$from' to '$now'" }

                    bright
                        .getReadings(BrightResourceType.GAS_CONSUMPTION, BrightPeriod.PT1H, from, now)
                        .readings
                        .filter { it.consumption != null }
                } else {
                    listOf()
                }

                val (oldReadings, last2Days) = readings.partition { it.time < now.minus(48.hours) }

                // Add readings older than 2 days to state
                val latest = oldReadings.maxOfOrNull { it.time } ?: state.latestReading
                val newTotal = state.meterTotal + oldReadings.mapNotNull { it.m3 }.sum()

                // Add all reading to temporary value
                val mostRecentBucket = readings.maxOfOrNull { it.time } ?: state.latestReading
                val tempTotal = newTotal + last2Days.mapNotNull { it.m3 }.sum()

                val next = Instant.fromEpochSeconds(nextHalfHour(now) + Random.Default.nextInt(120))

                logger.info { "Got ${readings.size} readings, Latest = $latest, Total = $newTotal, TempTotal = $tempTotal" }
                logger.info { readings.toString() }
                updateState(state.copy(nextCatchup = next, latestReading = latest, meterTotal = newTotal))
                bus.emit(BrightEvent(tempTotal, mostRecentBucket))
            }
        }
    }

    companion object : KLogging()
}
