package uk.co.thomasc.thealley.devices.relay

import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.cached
import uk.co.thomasc.thealley.client
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.generic.IAlleyRevocable
import uk.co.thomasc.thealley.devices.kasa.bulb.TriggerHelper
import uk.co.thomasc.thealley.devices.state.relay.RelayState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.types.RelayConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import kotlin.time.Duration.Companion.minutes

class RelayDevice(id: Int, config: RelayConfig, state: RelayState, stateStore: IStateUpdater<RelayState>) :
    AlleyDevice<RelayDevice, RelayConfig, RelayState>(id, config, state, stateStore), IAlleyRelay, IAlleyStats, IAlleyRevocable {

    private val triggerHelper = TriggerHelper(this) { this.state }

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
    private var powerState by cached(1.minutes, true) {
        client.get("http://${config.host}.light.kirkstall.top-cat.me/api/relay/0?apikey=${config.apiKey}") {
            accept(ContentType.Any)
        }.body<Int>() > 0
    }

    private suspend fun setLightState(bus: AlleyEventEmitter, state: Int) {
        bus.emit(MqttSendEvent("${config.host}/relay/0/set", "$state"))
    }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) = setLightState(bus, if (value) 1 else 0)

    override suspend fun getPowerState(): Boolean {
        updateState(state.copy(on = powerState))
        return state.on
    }

    override suspend fun togglePowerState(bus: AlleyEventEmitter) = setLightState(bus, 2)

    override suspend fun hold() {
        updateState(state.copy(ignoreMotionUntil = Clock.System.now().plus(config.switchTimeout)))
    }

    override suspend fun revoke() {
        updateState(state.copy(ignoreMotionUntil = null))
    }

    private suspend fun onWithNightScaling(bus: AlleyEventEmitter, now: Instant) =
        setPowerState(
            bus,
            NightBrightnessCalc.getBrightnessFor(now) > 50
        )

    override suspend fun init(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.LIGHT,
            false,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            )
        )

        bus.handle<MqttMessageEvent> { ev ->
            Regex("([^/,]+)/([^/,]+)(?:/([^/,]+))?").find(ev.topic)?.also {
                val (_, host, prop, _) = it.groupValues

                if (host == config.host) {
                    when (prop) {
                        "relay" -> {
                            powerState = ev.payload == "1"
                            if (updateState(state.copy(on = ev.payload == "1"))) {
                                bus.emit(ReportStateEvent(this))
                            }
                        }
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

        triggerHelper.init(bus, { now ->
            onWithNightScaling(bus, now)
        }) {
            setPowerState(bus, false)
        }
    }

    companion object : KLogging()
}
