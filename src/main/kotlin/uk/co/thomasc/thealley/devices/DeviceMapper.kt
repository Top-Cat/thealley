package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.runBlocking
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.ScenePart

open class DeviceMapper(
    private val localClient: LocalClient,
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    private fun getLight(id: Int) = switchRepository.getDeviceForId(id)

    protected fun SwitchRepository.Device.toLight(): Deferred<Light<*>?> {
        return when (type) {
            SwitchRepository.DeviceType.BULB -> localClient.getDevice(hostname).bulb { it }
            SwitchRepository.DeviceType.RELAY -> CompletableDeferred(relayClient.getRelay(hostname))
            else -> CompletableDeferred(null)
        }
    }

    fun List<ScenePart>.each(block: (Light<*>, ScenePart) -> Unit) {
        runBlocking {
            map {
                getLight(it.lightId) to it
            }.map {
                it.first.toLight() to it.second
            }.map {
                it.first.await()?.let {
                    light -> block(light, it.second)
                }
            }
        }
    }

}
