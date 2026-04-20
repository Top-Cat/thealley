package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.RelayStateEvent
import uk.co.thomasc.thealley.devices.state.esphome.MMWaveState
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.types.MMWaveConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice

class MMWaveDevice(id: Int, config: MMWaveConfig, state: MMWaveState, stateStore: IStateUpdater<MMWaveState>) :
    ZigbeeDevice<MMWaveUpdate, MMWaveDevice, MMWaveConfig, MMWaveState>(id, config, state, stateStore, MMWaveUpdate.serializer()), IAlleyStats {

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: MMWaveUpdate) {
        val lux = update.illuminance.toInt()
        if (updateState(state.copy(lightIntensity = lux))) {
            bus.emit(LuxEvent(id, lux))
        }

        if (updateState(state.copy(occupied = update.occupancy))) {
            // TODO: This is pretending to be a relay but the device / config is not a relay
            bus.emit(RelayStateEvent(id, update.occupancy))
        }

        props["temperature"] = JsonPrimitive(update.temperature.toDouble())
    }

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()

    companion object : KLogging()
}

data class LuxEvent(val deviceId: Int, val lux: Int) : IAlleyEvent {
    fun on() = lux > 100
}
