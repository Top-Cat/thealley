package uk.co.thomasc.thealley.devicev2.kasa.plug

import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.devices.EmeterResponse
import uk.co.thomasc.thealley.devices.PlugData
import uk.co.thomasc.thealley.devices.RealtimePower
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.kasa.KasaDevice
import uk.co.thomasc.thealley.devicev2.types.PlugConfig

class PlugDevice(id: Int, config: PlugConfig, state: PlugState, stateStore: IStateUpdater<PlugState>) :
    KasaDevice<PlugData, PlugDevice, PlugConfig, PlugState>(id, config, state, stateStore) {

    suspend fun getName() = getData()?.alias
    suspend fun getPowerState() = getData()?.getRelayState()
    suspend fun getUptime() = getData()?.on_time
    suspend fun getSignalStrength() = getData()?.rssi

    suspend fun getPower() =
        makeRequest {
            // May as well get sysinfo while we're at it
            send("{\"system\":{\"get_sysinfo\":{}},\"emeter\":{\"get_realtime\":{}}}")?.let {
                (parseSysInfo(it) as? PlugData)?.apply {
                    deviceData = this
                }

                alleyJson.decodeFromString<EmeterResponse>(it).emeter.get_realtime
            } ?: RealtimePower(0F, 0F, 0F, 0F, -1)
        }

    companion object : KLogging()
}
