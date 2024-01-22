package uk.co.thomasc.thealley.devices.system.sun

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.types.SunConfig
import java.util.Calendar

class SunDevice(id: Int, config: SunConfig, state: SunState, stateStore: IStateUpdater<SunState>) :
    AlleyDevice<SunDevice, SunConfig, SunState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<TickEvent> {
            val daytime = isDaytime()
            if (state.daytime != daytime) {
                bus.emit(if (daytime) SunRiseEvent else SunSetEvent)
                updateState(state.copy(daytime = daytime))
            }
        }
    }

    private val sunCalculator = SolarEventCalculator(Location(config.lat, config.lon), config.tz)

    private lateinit var sunsetCache: Pair<Instant, Instant>
    private var cacheTime: Calendar? = null

    private fun Calendar.toKInstant() = this.toInstant().toKotlinInstant()

    private fun isDaytime(): Boolean {
        val now = Calendar.getInstance()
        val nowInstant = now.toKInstant()

        if (now.get(Calendar.YEAR) != cacheTime?.get(Calendar.YEAR) || now.get(Calendar.DAY_OF_YEAR) != cacheTime?.get(Calendar.DAY_OF_YEAR)) {
            val sunrise = sunCalculator.computeSunriseCalendar(Zenith(89.0), now).toKInstant()
            val sunset = sunCalculator.computeSunsetCalendar(Zenith(89.0), now).toKInstant()

            sunsetCache = sunrise to sunset
            cacheTime = now
        }

        return sunsetCache.first < nowInstant && sunsetCache.second > nowInstant
    }
}
