package uk.co.thomasc.thealley.devicev2.energy.bright

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.TickEvent
import uk.co.thomasc.thealley.devicev2.energy.bright.client.Bright
import uk.co.thomasc.thealley.devicev2.energy.bright.client.BrightPeriod
import uk.co.thomasc.thealley.devicev2.energy.bright.client.BrightResourceType
import uk.co.thomasc.thealley.devicev2.types.BrightConfig
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

class BrightDevice(id: Int, config: BrightConfig, state: BrightState, stateStore: IStateUpdater<BrightState>) :
    AlleyDevice<BrightDevice, BrightConfig, BrightState>(id, config, state, stateStore) {

    private val bright = Bright(config.email, config.pass)
    private fun nextHalfHour(instant: Instant) = ((instant.epochSeconds / 1800) + 1) * 1800

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<TickEvent> {
            val now = Clock.System.now()
            if (state.nextCatchup?.let { now > it } != false) {
                val catchup = bright.catchup(BrightResourceType.GAS_CONSUMPTION)
                if (catchup.data.status != null) {
                    logger.info { "Bright catchup initiated ${catchup.data.status}" }
                }

                val from = state.latestReading.plus(1.hours)
                val to = now.minus(1.hours)

                val newReadings = if (from < to) {
                    logger.info { "Getting readings from '$from' to '$to'" }

                    bright
                        .getReadings(BrightResourceType.GAS_CONSUMPTION, BrightPeriod.PT1H, from, to)
                        .readings
                        .filter { it.consumption != null }
                        .dropLast(1) // Last value could be the current bucket
                } else {
                    listOf()
                }

                val consumption = newReadings.mapNotNull { it.m3 }.sum()
                val latest = newReadings.maxOfOrNull { it.time } ?: state.latestReading
                val next = Instant.fromEpochSeconds(nextHalfHour(now) + Random.Default.nextInt(120))
                val newTotal = state.meterTotal + consumption

                logger.info { "Got ${newReadings.size} new readings ${newReadings.map { it.consumption }}, Latest = $latest, Total = $newTotal" }
                updateState(state.copy(nextCatchup = next, latestReading = latest, meterTotal = newTotal))
                if (newReadings.isNotEmpty()) bus.emit(BrightEvent(newTotal, latest))
            }
        }
    }

    companion object : KLogging()
}
