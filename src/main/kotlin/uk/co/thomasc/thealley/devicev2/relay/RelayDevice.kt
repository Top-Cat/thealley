package uk.co.thomasc.thealley.devicev2.relay

import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.client.client
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.IAlleyStats
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devicev2.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devicev2.sun.SunRiseEvent
import uk.co.thomasc.thealley.devicev2.sun.SunSetEvent
import uk.co.thomasc.thealley.devicev2.types.RelayConfig

class RelayDevice(id: Int, config: RelayConfig, state: RelayState, stateStore: IStateUpdater<RelayState>) :
    AlleyDevice<RelayDevice, RelayConfig, RelayState>(id, config, state, stateStore), IAlleyLight, IAlleyStats {

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()

    private suspend fun setLightState(bus: AlleyEventBus, state: Int) {
        bus.emit(MqttSendEvent("${config.host}/relay/0/set", "$state"))
    }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) = setLightState(bus, if (value) 1 else 0)
    override suspend fun getLightState() = IAlleyLight.LightState(if (getPowerState()) 100 else 0, 0, 0, 0)

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let { b ->
            setPowerState(bus, b > 50)
        }
    }

    override suspend fun getPowerState() =
        try {
            client.get("http://${config.host}.light.kirkstall.top-cat.me/api/relay/0?apikey=${config.apiKey}") {
                accept(ContentType.Any)
            }.body<Int>() > 0
        } catch (e: Exception) {
            false
        }

    override suspend fun togglePowerState(bus: AlleyEventBus) = setLightState(bus, 2)
    override suspend fun revoke() {
        // TODO: revoke override so rules can change light state
    }

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            Regex("([^/,]+)/([^/,]+)(?:/([^/,]+))?").find(ev.topic)?.also {
                val (_, host, prop, _) = it.groupValues

                if (host == config.host) {
                    when (prop) {
                        "relay" -> updateState(state.copy(on = ev.payload == "1"))
                        "button" -> togglePowerState(bus)
                        else -> props[prop] = try {
                            JsonPrimitive(ev.payload.toDouble())
                        } catch (e: NumberFormatException) {
                            JsonPrimitive(ev.payload)
                        }
                    }
                }
            }
        }
        bus.handle<SunRiseEvent> {
            setLightState(bus, 0)
        }
        bus.handle<SunSetEvent> {
            setLightState(bus, 1)
        }
    }

    companion object : KLogging()
}
