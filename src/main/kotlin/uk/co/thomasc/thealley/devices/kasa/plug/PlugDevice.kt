package uk.co.thomasc.thealley.devices.kasa.plug

import mu.KLogging
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.kasa.KasaDevice
import uk.co.thomasc.thealley.devices.kasa.plug.data.PlugData
import uk.co.thomasc.thealley.devices.kasa.plug.data.PlugResponse
import uk.co.thomasc.thealley.devices.kasa.plug.data.RealtimePower
import uk.co.thomasc.thealley.devices.types.PlugConfig

class PlugDevice(id: Int, config: PlugConfig, state: PlugState, stateStore: IStateUpdater<PlugState>) :
    KasaDevice<PlugData, PlugDevice, PlugConfig, PlugState>(id, config, state, stateStore) {

    suspend fun getName() = getData<PlugResponse, PlugData>()?.alias
    suspend fun getPowerState() = getData<PlugResponse, PlugData>()?.getRelayState()
    suspend fun getUptime() = getData<PlugResponse, PlugData>()?.onTime
    suspend fun getSignalStrength() = getData<PlugResponse, PlugData>()?.rssi

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
