package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.client.alleyJson
import kotlin.time.Duration.Companion.seconds

class Plug(host: String) : KasaDevice<PlugData>(host) {

    private var plugData: PlugData? = null
    private val mutex = Mutex()

    override suspend fun getData() = mutex.withLock {
        updateData()
    }

    private var lastRequest = Instant.DISTANT_PAST
    suspend fun updateData() =
        run {
            if (Clock.System.now().minus(20.seconds) > lastRequest) {
                (getSysInfo(host, 3000) as? PlugData)?.apply {
                    plugData = this
                }
            } else plugData
        }.also {
            lastRequest = Clock.System.now()
        }

    suspend fun getName() = getData()?.alias
    suspend fun getPowerState() = getData()?.relay_state
    suspend fun getUptime() = getData()?.on_time
    suspend fun getSignalStrength() = getData()?.rssi

    suspend fun getPower() =
        mutex.withLock {
            run {
                // May as well get sysinfo while we're at it
                send("{\"system\":{\"get_sysinfo\":{}},\"emeter\":{\"get_realtime\":{}}}")?.let {
                    (parseSysInfo(it) as? PlugData)?.apply {
                        plugData = this
                    }

                    alleyJson.decodeFromString<EmeterResponse>(it).emeter.get_realtime
                } ?: RealtimePower(0F, 0F, 0F, 0F, -1)
            }.also {
                lastRequest = Clock.System.now()
            }
        }

    /*fun setPowerState(value: Boolean): Plug =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    private suspend fun setPlugState(state: BulbUpdate): Plug {
        val obj = LightingServiceUpdate(LightingService(state))
        val response = send(mapper.writeValueAsString(obj))

        val result = mapper.readValue<LightingServiceUpdate>(response)

        return Plug(client, host, bulb.copy(light_state = result.lightingService.transition_light_state))
    }*/

    private suspend fun send(json: String) = send(json, host, timeout = 500)
}
