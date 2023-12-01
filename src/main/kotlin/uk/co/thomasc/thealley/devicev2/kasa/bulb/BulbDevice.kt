package uk.co.thomasc.thealley.devicev2.kasa.bulb

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.TickEvent
import uk.co.thomasc.thealley.devicev2.kasa.KasaDevice
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.BulbData
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.BulbRealtimePower
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.BulbResponse
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.BulbUpdate
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.LightingService
import uk.co.thomasc.thealley.devicev2.kasa.bulb.data.LightingServiceUpdate
import uk.co.thomasc.thealley.devicev2.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devicev2.system.sun.SunRiseEvent
import uk.co.thomasc.thealley.devicev2.system.sun.SunSetEvent
import uk.co.thomasc.thealley.devicev2.types.BulbConfig
import uk.co.thomasc.thealley.devicev2.xiaomi.aq2.MotionEvent

class BulbDevice(id: Int, config: BulbConfig, state: BulbState, stateStore: IStateUpdater<BulbState>) :
    KasaDevice<BulbData, BulbDevice, BulbConfig, BulbState>(id, config, state, stateStore), IAlleyLight {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<SunRiseEvent> {
            updateState(state.copy(daytime = true))
        }
        bus.handle<SunSetEvent> {
            val now = Clock.System.now()
            val off = state.lastMotion?.plus(config.timeout)?.let {
                if (it > now) {
                    onWithNightScaling(now)
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
                setLightState(BulbUpdate(false))
                updateState(state.copy(offAt = null))
            }
        }
        bus.handle<MotionEvent> { ev ->
            val now = Clock.System.now()
            if (state.ignoreMotionUntil?.let { it > now } == true || !config.sensors.contains(ev.id)) return@handle

            updateState(state.copy(lastMotion = now, offAt = now.plus(config.timeout)))
            if (!state.daytime) {
                onWithNightScaling(now)
            }
        }
    }

    private suspend fun onWithNightScaling(now: Instant, transitionTime: Int = 500) =
        setComplexState(
            IAlleyLight.LightState(
                NightBrightnessCalc.getBrightnessFor(now)
            ),
            transitionTime
        )

    suspend fun getName() = getData<BulbResponse, BulbData>()?.alias
    override suspend fun getLightState() = getData<BulbResponse, BulbData>()?.lightState.let {
        IAlleyLight.LightState(it?.brightness, it?.hue, it?.saturation, it?.temperature)
    }
    suspend fun getSignalStrength() = getData<BulbResponse, BulbData>()?.rssi
    suspend fun getPowerUsage() = getPower().power

    private suspend fun setLightState(state: BulbUpdate) {
        val obj = LightingServiceUpdate(LightingService(state))

        send(obj)?.let {
            val result = alleyJson.decodeFromString<LightingServiceUpdate>(it)
            deviceData = deviceData?.copy(lightState = result.lightingService.transitionLightState)
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

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) = setLightState(BulbUpdate(value))

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        updateState(state.copy(ignoreMotionUntil = Clock.System.now().plus(config.switchTimeout)))
        setComplexState(lightState, transitionTime)
    }

    private suspend fun setComplexState(lightState: IAlleyLight.LightState, transitionTime: Int?) {
        setLightState(
            lightState.temperature?.let {
                BulbUpdate(transitionTime, true, null, null, null, lightState.brightness, lightState.temperature)
            } ?: lightState.hue?.let {
                BulbUpdate(transitionTime, true, null, lightState.hue, lightState.saturation, lightState.brightness, 0)
            } ?: BulbUpdate(transitionTime, (lightState.brightness ?: 1) > 0, null, null, null, lightState.brightness, null)
        )
    }

    override suspend fun getPowerState() = getData<BulbResponse, BulbData>()?.lightState?.isOn() == true

    override suspend fun togglePowerState(bus: AlleyEventBus) =
        setPowerState(bus, !getPowerState())

    override suspend fun revoke() {
        updateState(state.copy(ignoreMotionUntil = null))
    }

    suspend fun getModel() = getData<BulbResponse, BulbData>()?.model
    suspend fun getHwVer() = getData<BulbResponse, BulbData>()?.hwVer
    suspend fun getSwVer() = getData<BulbResponse, BulbData>()?.swVer

    companion object : KLogging()
}
