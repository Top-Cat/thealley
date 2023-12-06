package uk.co.thomasc.thealley.devices.xiaomi.aq2

import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.types.MotionConfig
import kotlin.collections.set

class MotionDevice(id: Int, config: MotionConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<MotionDevice, MotionConfig, EmptyState>(id, config, state, stateStore), IAlleyStats {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val host = ev.topic.substring(0, ev.topic.indexOf("/"))
            if (host != "zigbee") return@handle

            val deviceId = ev.topic.substring(host.length + 1)
            if (deviceId != config.deviceId) return@handle

            val update = alleyJson.decodeFromString<MotionSensorUpdate>(ev.payload)

            if (update.occupancy) {
                trigger(bus)
            }

            props["illuminance"] = JsonPrimitive(update.illuminance.toDouble())
            update.battery?.let {
                props["battery"] = JsonPrimitive(update.battery.toDouble())
            }
            props["voltage"] = JsonPrimitive(update.voltage.toDouble())
        }
    }

    suspend fun trigger(bus: AlleyEventBus) {
        bus.emit(MotionEvent(id, config.deviceId))
    }

    companion object : KLogging()

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
}
