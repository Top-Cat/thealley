package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository

class DeviceMapper(
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository
) {

    private fun getLight(id: Int) = switchRepository.getDeviceForId(id)
    private val bulbMap = mutableMapOf<String, Bulb>()

    private fun SwitchRepository.Device.innerToLight(): Light<*>? {
        return when (type) {
            SwitchRepository.DeviceType.BULB -> bulbMap.getOrPut(hostname) {
                Bulb(hostname)
            }
            SwitchRepository.DeviceType.RELAY -> relayClient.getRelay(hostname)
            SwitchRepository.DeviceType.BLIND -> relayClient.getBlind(hostname)
            else -> null
        }
    }

    fun toLight(device: SwitchRepository.Device) = device.innerToLight()

    suspend fun <T : HasDeviceId, S : Any> each(sceneParts: List<T>, block: suspend (Light<*>, T) -> S?) =
        sceneParts.innerEach(block)

    interface HasDeviceId {
        val deviceId: Int
    }

    private suspend fun <T : HasDeviceId, S : Any> List<T>.innerEach(block: suspend (Light<*>, T) -> S?) =
        map {
            getLight(it.deviceId) to it
        }.asFlow().flatMapMerge(10) {
            flow {
                emit(it.first.innerToLight() to it.second)
            }
        }.flatMapMerge(10) {
            flow {
                try {
                    it.first?.let { light ->
                        val result = block(light, it.second)
                        if (result != null) emit(result)
                    }
                } catch (e: KotlinNullPointerException) {
                    // Ignore
                }
            }
        }.toList()
}
