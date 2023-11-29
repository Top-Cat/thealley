package uk.co.thomasc.thealley.devicev2.kasa.bulb

import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devices.BulbData
import uk.co.thomasc.thealley.devices.BulbEmeterResponse
import uk.co.thomasc.thealley.devices.BulbRealtimePower
import uk.co.thomasc.thealley.devices.BulbUpdate
import uk.co.thomasc.thealley.devices.LightingService
import uk.co.thomasc.thealley.devices.LightingServiceUpdate
import uk.co.thomasc.thealley.devicev2.AlleyEventBus
import uk.co.thomasc.thealley.devicev2.IAlleyLight
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.kasa.KasaDevice
import uk.co.thomasc.thealley.devicev2.sun.SunRiseEvent
import uk.co.thomasc.thealley.devicev2.sun.SunSetEvent
import uk.co.thomasc.thealley.devicev2.types.BulbConfig

class BulbDevice(id: Int, config: BulbConfig, state: BulbState, stateStore: IStateUpdater<BulbState>) :
    KasaDevice<BulbData, BulbDevice, BulbConfig, BulbState>(id, config, state, stateStore), IAlleyLight {

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<SunRiseEvent> {
            setLightState(BulbUpdate(false))
        }
        bus.handle<SunSetEvent> {
            setLightState(BulbUpdate(true))
        }
    }

    suspend fun getName() = getData()?.alias
    override suspend fun getLightState() = getData()?.light_state.let {
        IAlleyLight.LightState(it?.brightness, it?.hue, it?.saturation, it?.color_temp)
    }
    suspend fun getSignalStrength() = getData()?.rssi
    suspend fun getPowerUsage() = getPower().power_mw

    private suspend fun setLightState(state: BulbUpdate) {
        val obj = LightingServiceUpdate(LightingService(state))

        send(obj)?.let {
            val result = alleyJson.decodeFromString<LightingServiceUpdate>(it)
            deviceData = getData()?.copy(light_state = result.lightingService.transition_light_state)
        }
    }

    private suspend fun getPower() =
        makeRequest {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                (parseSysInfo(it) as? BulbData)?.apply {
                    deviceData = this
                }

                alleyJson.decodeFromString<BulbEmeterResponse>(it).emeter.get_realtime
            } ?: BulbRealtimePower(0, -1)
        }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) = setLightState(BulbUpdate(value))

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let {
            setPowerState(bus, it > 50)
        }
    }

    override suspend fun getPowerState() = getData()?.light_state?.isOn() == true

    override suspend fun togglePowerState(bus: AlleyEventBus) =
        setPowerState(bus, !getPowerState())

    override suspend fun revoke() {
        // TODO: revoke override so rules can change light state
    }

    suspend fun getModel() = getData()?.model
    suspend fun getHwVer() = getData()?.hw_ver
    suspend fun getSwVer() = getData()?.sw_ver

    companion object : KLogging()
}
