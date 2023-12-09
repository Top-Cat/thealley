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
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.TickEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devices.system.sun.SunSetEvent
import uk.co.thomasc.thealley.devices.types.RelayConfig
import uk.co.thomasc.thealley.devices.xiaomi.aq2.MotionEvent
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import kotlin.time.Duration.Companion.minutes

class RelayDevice(id: Int, config: RelayConfig, state: RelayState, stateStore: IStateUpdater<RelayState>) :
    AlleyDevice<RelayDevice, RelayConfig, RelayState>(id, config, state, stateStore), IAlleyLight, IAlleyStats {

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
    private var powerState by cached(1.minutes) {
        client.get("http://${config.host}.light.kirkstall.top-cat.me/api/relay/0?apikey=${config.apiKey}") {
            accept(ContentType.Any)
        }.body<Int>() > 0
    }

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

    override suspend fun getPowerState(): Boolean {
        updateState(state.copy(on = powerState))
        return state.on
    }

    override suspend fun togglePowerState(bus: AlleyEventBus) = setLightState(bus, 2)

    override suspend fun hold() {
        // TODO: revoke override so rules can change light state
    }

    override suspend fun revoke() {
        // TODO: revoke override so rules can change light state
    }

    private suspend fun onWithNightScaling(bus: AlleyEventBus, now: Instant) =
        setPowerState(
            bus,
            NightBrightnessCalc.getBrightnessFor(now) > 50
        )

    override suspend fun init(bus: AlleyEventBus) {
        registerGoogleHomeDevice(
            DeviceType.LIGHT,
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
                            updateState(state.copy(on = ev.payload == "1"))
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

        bus.handle<SunRiseEvent> {
            updateState(state.copy(daytime = true))
        }
        bus.handle<SunSetEvent> {
            val now = Clock.System.now()
            val off = state.lastMotion?.plus(config.timeout)?.let {
                if (it > now) {
                    onWithNightScaling(bus, now)
                    it
                } else {
                    null
                }
            }
            updateState(state.copy(daytime = false, offAt = off))
        }
        bus.handle<TickEvent> {
            val now = Clock.System.now()
            if (state.offAt?.let { now > it } == true) {
                setPowerState(bus, false)
                updateState(state.copy(offAt = null))
            }
        }
        bus.handle<MotionEvent> { ev ->
            val now = Clock.System.now()
            if (state.ignoreMotionUntil?.let { it > now } == true || !config.sensors.contains(ev.id)) return@handle

            updateState(state.copy(lastMotion = now, offAt = now.plus(config.timeout)))
            if (!state.daytime) {
                onWithNightScaling(bus, now)
            }
        }
    }

    companion object : KLogging()
}
