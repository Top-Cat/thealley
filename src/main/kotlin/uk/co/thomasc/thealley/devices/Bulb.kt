package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import mu.KLogging
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.client.alleyJsonUgly
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class Bulb(host: String) : KasaDevice<BulbData>(host), Light<Bulb> {

    companion object : KLogging()

    private var bulbData: BulbData? = null
    private val mutex = Mutex()

    override suspend fun getData() = mutex.withLock {
        updateData()
    }

    private var lastRequest = Instant.DISTANT_PAST
    private suspend fun updateData() =
        run {
            if (Clock.System.now().minus(20.seconds) > lastRequest) {
                (getSysInfo(host, 3000) as? BulbData)?.apply {
                    bulbData = this
                }
            } else bulbData
        }.also {
            lastRequest = Clock.System.now()
        }

    suspend fun getName() = getData()?.alias
    override suspend fun getPowerState() = getData()?.light_state?.isOn() == true
    suspend fun getLightState() = getData()?.light_state
    suspend fun getSignalStrength() = getData()?.rssi
    suspend fun getPowerUsage() = getPower().power_mw

    suspend fun getHwVer() = getData()?.hw_ver
    suspend fun getSwVer() = getData()?.sw_ver
    suspend fun getModel() = getData()?.model

    private suspend fun getPower() =
        mutex.withLock {
            run {
                // May as well get sysinfo while we're at it
                send("{\"system\":{\"get_sysinfo\":{}},\"smartlife.iot.common.emeter\":{\"get_realtime\":{}}}")?.let {
                    (parseSysInfo(it) as? BulbData)?.apply {
                        bulbData = this
                    }

                    alleyJson.decodeFromString<BulbEmeterResponse>(it).emeter?.get_realtime
                } ?: BulbRealtimePower(0, -1)
            }.also {
                lastRequest = Clock.System.now()
            }
        }

    override fun setPowerState(value: Boolean): Bulb =
        runBlocking {
            setLightState(BulbUpdate(value))
        }

    override suspend fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) =
        setLightState(
            temperature?.let {
                BulbUpdate(transitionTime, true, null, null, null, brightness, temperature)
            } ?: hue?.let {
                BulbUpdate(transitionTime, true, null, hue, saturation, brightness, 0)
            } ?: BulbUpdate(transitionTime, (brightness ?: 1) > 0, null, null, null, brightness, null)
        )

    private suspend fun setLightState(state: BulbUpdate): Bulb {
        val obj = LightingServiceUpdate(LightingService(state))

        send(obj)?.let {
            val result = alleyJson.decodeFromString<LightingServiceUpdate>(it)
            bulbData = getData()?.copy(light_state = result.lightingService.transition_light_state)
        }

        return this
    }

    override suspend fun togglePowerState() =
        setPowerState(!getPowerState())

    private suspend inline fun <reified T> send(obj: T) = send(alleyJsonUgly.encodeToString(obj), host, timeout = 500)
    private suspend fun send(json: String) = send(json, host, timeout = 500)
}
