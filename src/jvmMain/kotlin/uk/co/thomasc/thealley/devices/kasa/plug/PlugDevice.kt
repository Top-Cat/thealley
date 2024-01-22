package uk.co.thomasc.thealley.devices.kasa.plug

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.kasa.plug.data.PlugData
import uk.co.thomasc.thealley.devices.kasa.plug.data.PlugResponse
import uk.co.thomasc.thealley.devices.kasa.plug.data.RealtimePower
import uk.co.thomasc.thealley.devices.types.PlugConfig

class PlugDevice(id: Int, config: PlugConfig, state: PlugState, stateStore: IStateUpdater<PlugState>) :
    KasaDevice<PlugData, PlugDevice, PlugConfig, PlugState>(id, config, state, stateStore), IAlleyRelay {

    suspend fun getName() = getData<PlugResponse>()?.alias
    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) {
        // Not implemented
    }

    override suspend fun getPowerState() = getData<PlugResponse>()?.getRelayState() ?: false
    override suspend fun togglePowerState(bus: AlleyEventBus) = setPowerState(bus, !getPowerState())

    suspend fun getUptime() = getData<PlugResponse>()?.onTime
    suspend fun getSignalStrength() = getData<PlugResponse>()?.rssi

    suspend fun getPower() =
        makeRequest {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"emeter\":{\"get_realtime\":{}}}")?.let {
                parseSysInfo<PlugResponse, PlugData>(it)?.let { response ->
                    deviceData = response.system.sysInfo
                    response.emeter?.realtime
                }
            } ?: RealtimePower(0F, 0F, 0F, 0F, -1)
        }

    companion object : KLogging()
}
