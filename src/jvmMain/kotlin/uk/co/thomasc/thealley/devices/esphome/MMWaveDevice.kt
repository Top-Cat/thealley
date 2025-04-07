package uk.co.thomasc.thealley.devices.esphome

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.RelayStateEvent
import uk.co.thomasc.thealley.devices.state.esphome.MMWaveState
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.types.MMWaveConfig

class MMWaveDevice(id: Int, config: MMWaveConfig, state: MMWaveState, stateStore: IStateUpdater<MMWaveState>) :
    AlleyDevice<MMWaveDevice, MMWaveConfig, MMWaveState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBusShim) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 4 || parts[0] != config.prefix || parts[3] != "state") return@handle

            // State update
            when (parts[2]) {
                "light_intensity" -> (ev.payload.toIntOrNull() ?: 0).let { lux ->
                    if (updateState(state.copy(lightIntensity = lux))) {
                        bus.emit(LuxEvent(id, lux))
                    }
                }
                "movement" -> (ev.payload == "ON").let { occupied ->
                    if (updateState(state.copy(occupied = occupied))) {
                        // TODO: This is pretending to be a relay but the device / config is not a relay
                        bus.emit(RelayStateEvent(id, ev.payload == "ON"))
                    }
                }
                // "occupancy"
            }
        }
    }

    companion object : KLogging()
}

data class LuxEvent(val deviceId: Int, val lux: Int) : IAlleyEvent {
    fun on() = lux > 100
}
