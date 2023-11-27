package uk.co.thomasc.thealley.devicev2.relay

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.RelayConfig
import uk.co.thomasc.thealley.devicev2.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devicev2.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devicev2.sun.SunRiseEvent
import uk.co.thomasc.thealley.devicev2.sun.SunSetEvent

class RelayDevice(config: RelayConfig, state: RelayState, stateStore: IStateUpdater<RelayState>) :
    AlleyDevice<RelayDevice, RelayConfig, RelayState>(config, state, stateStore) {

    private val props: MutableMap<String, JsonPrimitive> = mutableMapOf()

    private suspend fun setLightState(state: Int, bus: AlleyEventBus) {
        bus.emit(MqttSendEvent("${config.host}/relay/0/set", "$state"))
    }

    private suspend fun togglePowerState(bus: AlleyEventBus) = setLightState(2, bus)

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val topic = ev.topic
            val message = ev.payload

            Regex("([^/,]+)/([^/,]+)(?:/([^/,]+))?").find(topic)?.also {
                val (str, host, prop, idx) = it.groupValues

                when (prop) {
                    "relay" -> updateState(state.copy(on = message == "1"))
                    "button" -> {
                        runBlocking {
                            togglePowerState(bus)
                        }
                    }
                    else -> props[prop] = try {
                        JsonPrimitive(message.toDouble())
                    } catch (e: NumberFormatException) {
                        JsonPrimitive(message)
                    }
                }
            } ?: logger.warn { "Couldn't parse MQTT message $topic - $message" }
        }
        bus.handle<SunRiseEvent> {
            setLightState(1, bus)
        }
        bus.handle<SunSetEvent> {
            setLightState(0, bus)
        }
    }

    companion object : KLogging()
}
