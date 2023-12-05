package uk.co.thomasc.thealley.devicev2.relay

import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.client.client
import uk.co.thomasc.thealley.devicev2.AlleyDevice
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.IAlleyStats
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.TickEvent
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devicev2.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devicev2.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devicev2.system.sun.SunSetEvent
import uk.co.thomasc.thealley.devicev2.types.RelayConfig
import uk.co.thomasc.thealley.devicev2.xiaomi.aq2.MotionEvent
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import kotlin.time.Duration.Companion.seconds

class RelayDevice(id: Int, config: RelayConfig, state: RelayState, stateStore: IStateUpdater<RelayState>) :
    AlleyDevice<RelayDevice, RelayConfig, RelayState>(id, config, state, stateStore), IAlleyLight, IAlleyStats {

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()
    private var lastRequest = Instant.DISTANT_PAST
    private val mutex = Mutex()

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

    override suspend fun getPowerState() = mutex.withLock {
        if (Clock.System.now().minus(20.seconds) > lastRequest) {
            try {
                client.get("http://${config.host}.light.kirkstall.top-cat.me/api/relay/0?apikey=${config.apiKey}") {
                    accept(ContentType.Any)
                }.body<Int>() > 0
            } catch (e: Exception) {
                false
            }.also {
                updateState(state.copy(on = it))
            }
        }

        lastRequest = Clock.System.now()
        state.on
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
