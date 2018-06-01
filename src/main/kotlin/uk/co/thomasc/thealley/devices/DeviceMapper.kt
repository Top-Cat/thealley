package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository

class DeferredLight(var obj: Any?) {
    fun resolve(): Light<*>? {
        return runBlocking {
            obj.let {
                when (it) {
                    is Deferred<*> -> it.await()
                    else -> obj
                }.let {
                    when (it) {
                        is Light<*> -> it
                        else -> null
                    }
                }
            }
        }
    }
}

@Component
class DeviceMapper(
    private val localClient: LocalClient,
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    private fun getLight(id: Int) = switchRepository.getDeviceForId(id)

    private fun SwitchRepository.Device.innerToLight(): DeferredLight {
        return DeferredLight(when (type) {
            SwitchRepository.DeviceType.BULB -> localClient.getDevice(hostname).bulb { it }
            SwitchRepository.DeviceType.RELAY -> relayClient.getRelay(hostname)
            else -> null
        })
    }

    fun toLight(device: SwitchRepository.Device) = device.innerToLight()

    fun <T: HasDeviceId> each(sceneParts: List<T>, block: (Light<*>, T) -> Unit) {
        sceneParts.innerEach(block)
    }

    interface HasDeviceId {
        val deviceId: Int
    }

    private fun <T: HasDeviceId> List<T>.innerEach(block: (Light<*>, T) -> Unit) {
        runBlocking {
            map {
                getLight(it.deviceId) to it
            }.map {
                it.first.innerToLight() to it.second
            }.map {
                it.first.resolve()?.let {
                    light -> block(light, it.second)
                }
            }
        }
    }

}
