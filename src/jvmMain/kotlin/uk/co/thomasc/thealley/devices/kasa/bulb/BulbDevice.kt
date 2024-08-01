package uk.co.thomasc.thealley.devices.kasa.bulb

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.generic.IAlleyRevocable
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.kasa.bulb.data.BulbData
import uk.co.thomasc.thealley.devices.kasa.bulb.data.BulbRealtimePower
import uk.co.thomasc.thealley.devices.kasa.bulb.data.BulbResponse
import uk.co.thomasc.thealley.devices.kasa.bulb.data.BulbUpdate
import uk.co.thomasc.thealley.devices.kasa.bulb.data.LightingService
import uk.co.thomasc.thealley.devices.kasa.bulb.data.LightingServiceUpdate
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devices.system.sun.SunSetEvent
import uk.co.thomasc.thealley.devices.types.BulbConfig
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionEvent
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.ColorSettingTrait
import uk.co.thomasc.thealley.google.trait.IColorState
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class BulbDevice(id: Int, config: BulbConfig, state: BulbState, stateStore: IStateUpdater<BulbState>) :
    KasaDevice<BulbData, BulbDevice, BulbConfig, BulbState>(id, config, state, stateStore), IAlleyLight, IAlleyRevocable {

    override suspend fun init(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.LIGHT,
            true,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus.bus, it)
                }
            ),
            BrightnessTrait(
                getBrightness = {
                    getLightPossibleState()?.brightness ?: 0
                },
                setBrightness = { b ->
                    setComplexState(bus.bus, IAlleyLight.LightState(b))
                }
            ),
            ColorSettingTrait(
                getColor = {
                    IColorState.fromLightState(getLightPossibleState())
                },
                setColor = {
                    it.setComplexState(bus.bus, ::setComplexState)
                }
            )
        )

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
                setLightState(bus, BulbUpdate(false))
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

    private suspend fun onWithNightScaling(bus: AlleyEventEmitter, now: Instant, transitionTime: Int = 500) =
        setComplexState(
            bus,
            IAlleyLight.LightState(
                NightBrightnessCalc.getBrightnessFor(now)
            ),
            transitionTime
        )

    suspend fun getName() = getData<BulbResponse>()?.alias
    override suspend fun getLightState() = getData<BulbResponse>()?.lightState.let {
        IAlleyLight.LightState(it?.brightness, it?.hue, it?.saturation, it?.temperature)
    }
    private suspend fun getLightPossibleState() = getData<BulbResponse>()?.lightState?.let {
        if (it.isOn()) it else it.dftOnState
    }
    suspend fun getSignalStrength() = getData<BulbResponse>()?.rssi
    suspend fun getPowerUsage() = getPower().power

    private suspend fun setLightState(bus: AlleyEventEmitter, update: BulbUpdate) {
        val obj = LightingServiceUpdate(LightingService(update))

        send(obj)?.let {
            val result = alleyJson.decodeFromString<LightingServiceUpdate>(it)
            deviceData = deviceData?.copy(lightState = result.lightingService.transitionLightState)
            bus.emit(ReportStateEvent(this))
        }
    }

    private suspend fun getPower() =
        makeRequest {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                parseSysInfo<BulbResponse, BulbData>(it)?.let { response ->
                    deviceData = response.system.sysInfo
                    response.emeter?.realtime
                }
            } ?: BulbRealtimePower(0, 0, -1)
        }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) = setLightState(bus, BulbUpdate(value))

    override suspend fun setComplexState(bus: AlleyEventEmitter, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        setLightState(
            bus,
            lightState.temperature?.let {
                BulbUpdate(transitionTime, true, null, null, null, lightState.brightness, lightState.temperature)
            } ?: lightState.hue?.let {
                BulbUpdate(transitionTime, true, null, lightState.hue, lightState.saturation, lightState.brightness, 0)
            } ?: BulbUpdate(transitionTime, (lightState.brightness ?: 1) > 0, null, null, null, lightState.brightness, null)
        )
    }

    override suspend fun getPowerState() = getData<BulbResponse>()?.lightState?.isOn() == true

    override suspend fun togglePowerState(bus: AlleyEventEmitter) =
        setPowerState(bus, !getPowerState())

    override suspend fun hold() {
        updateState(state.copy(ignoreMotionUntil = Clock.System.now().plus(config.switchTimeout)))
    }

    override suspend fun revoke() {
        updateState(state.copy(ignoreMotionUntil = null))
    }

    suspend fun getModel() = getData<BulbResponse>()?.model
    suspend fun getHwVer() = getData<BulbResponse>()?.hwVer
    suspend fun getSwVer() = getData<BulbResponse>()?.swVer

    companion object : KLogging()
}
