package uk.co.thomasc.thealley.devicev2.xiaomi.aq2

import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.client.MotionSensorUpdate
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IAlleyStats
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devicev2.types.MotionConfig
import kotlin.collections.set

class MotionDevice(id: Int, config: MotionConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<MotionDevice, MotionConfig, EmptyState>(id, config, state, stateStore), IAlleyStats {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val host = ev.topic.substring(0, ev.topic.indexOf("/"))
            if (host != "zigbee") return@handle

            val deviceId = ev.topic.substring(host.length + 1)
            if (deviceId != config.deviceId) return@handle

            val update = alleyJson.decodeFromString<MotionSensorUpdate>(ev.payload)

            if (update.occupancy) {
                bus.emit(MotionEvent(config.deviceId))
            }

            props["illuminance"] = JsonPrimitive(update.illuminance.toDouble())
            update.battery?.let {
                props["battery"] = JsonPrimitive(update.battery.toDouble())
            }
            props["voltage"] = JsonPrimitive(update.voltage.toDouble())
        }
    }

    companion object : KLogging()

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
}
