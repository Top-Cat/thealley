package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository

@Component
class DeviceMapper(
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    private fun getLight(id: Int) = switchRepository.getDeviceForId(id)
    private val bulbMap = mutableMapOf<String, Bulb>()

    private fun SwitchRepository.Device.innerToLight(): Light<*>? {
        return when (type) {
            SwitchRepository.DeviceType.BULB -> bulbMap.getOrPut(hostname) {
                Bulb(hostname).apply {
                    async(CommonPool) {
                        getName()
                    }
                }
            }
            SwitchRepository.DeviceType.RELAY -> relayClient.getRelay(hostname)
            else -> null
        }
    }

    fun toLight(device: SwitchRepository.Device) = device.innerToLight()

    fun <T: HasDeviceId, S: Any> each(sceneParts: List<T>, block: (Light<*>, T) -> S?) =
        sceneParts.innerEach(block)

    interface HasDeviceId {
        val deviceId: Int
    }

    private fun <T: HasDeviceId, S: Any> List<T>.innerEach(block: (Light<*>, T) -> S?) =
        runBlocking {
            map {
                getLight(it.deviceId) to it
            }.map {
                it.first.innerToLight() to it.second
            }.mapNotNull {
                try {
                    it.first?.let {
                        light -> block(light, it.second)
                    }
                } catch (e: KotlinNullPointerException) {
                    null
                }
            }
        }

}
