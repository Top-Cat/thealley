package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.runBlocking
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.ScenePart

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

open class DeviceMapper(
    private val localClient: LocalClient,
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    private fun getLight(id: Int) = switchRepository.getDeviceForId(id)

    protected fun SwitchRepository.Device.toLight(): DeferredLight {
        return DeferredLight(when (type) {
            SwitchRepository.DeviceType.BULB -> localClient.getDevice(hostname).bulb { it }
            SwitchRepository.DeviceType.RELAY -> relayClient.getRelay(hostname)
            else -> null
        })
    }

    fun List<ScenePart>.each(block: (Light<*>, ScenePart) -> Unit) {
        runBlocking {
            map {
                getLight(it.lightId) to it
            }.map {
                it.first.toLight() to it.second
            }.map {
                it.first.resolve()?.let {
                    light -> block(light, it.second)
                }
            }
        }
    }

}
