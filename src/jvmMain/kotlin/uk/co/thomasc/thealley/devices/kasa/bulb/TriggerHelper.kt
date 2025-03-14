package uk.co.thomasc.thealley.devices.kasa.bulb

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.state.kasa.bulb.ITriggerableState
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devices.system.sun.SunSetEvent
import uk.co.thomasc.thealley.devices.types.ITriggerableConfig
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionEvent

class TriggerHelper<U : ITriggerableState<U>, T : ITriggerableConfig<U>>(val device: AlleyDevice<*, T, U>, val getState: () -> U) {
    suspend fun init(bus: AlleyEventBusShim, onBlock: suspend (Instant) -> Unit, offBlock: suspend () -> Unit) {
        bus.handle<SunRiseEvent> {
            device.updateState(getState().toDaytime())
        }
        bus.handle<SunSetEvent> {
            val now = Clock.System.now()
            val off = getState().lastMotion?.plus(device.config.timeout)?.let {
                if (it > now) {
                    onBlock(now)
                    it
                } else {
                    null
                }
            }
            device.updateState(getState().toNighttime(off))
        }
        bus.handle<TickEvent> { ev ->
            if (getState().offAt?.let { ev.now > it } == true) {
                offBlock()
                device.updateState(getState().clearOffAt())
            }
        }
        bus.handle<MotionEvent> { ev ->
            val now = Clock.System.now()
            if (getState().ignoreMotionUntil?.let { it > now } == true || !device.config.sensors.contains(ev.id)) return@handle

            device.updateState(getState().motion(now, now.plus(device.config.timeout)))
            if (!getState().daytime) {
                onBlock(now)
            }
        }
    }
}
