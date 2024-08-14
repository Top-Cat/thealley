package uk.co.thomasc.thealley.devices.zigbee.aq2

import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.types.MotionConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import kotlin.collections.set

class MotionDevice(id: Int, config: MotionConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>, val dev: AlleyDeviceMapper) :
    ZigbeeDevice<MotionSensorUpdate, MotionDevice, MotionConfig, EmptyState>(id, config, state, stateStore, MotionSensorUpdate.serializer()), IAlleyStats {

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: MotionSensorUpdate) {
        if (update.occupancy) {
            trigger(bus)
        }

        props["illuminance"] = JsonPrimitive(update.illuminance.toDouble())
        update.battery?.let {
            props["battery"] = JsonPrimitive(update.battery.toDouble())
        }
        props["voltage"] = JsonPrimitive(update.voltage.toDouble())
    }

    suspend fun trigger(bus: AlleyEventEmitter) {
        bus.emit(MotionEvent(id, config.deviceId))
    }

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
}
