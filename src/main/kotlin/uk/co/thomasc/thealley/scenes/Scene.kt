package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SwitchRepository

data class ScenePart(val lightId: Int, val brightness: Int, val hue: Int)

class Scene(
    localClient: LocalClient,
    relayClient: RelayClient,
    switchRepository: SwitchRepository,
    private val lights: List<ScenePart>
) : DeviceMapper(localClient, relayClient, switchRepository) {

    fun execute() {
        lights.each {
            bulb, it ->

            when (it.brightness) {
                0 -> bulb.setPowerState(false)
                else -> bulb.setComplexState(it.brightness, it.hue)
            }
        }
    }

    fun off() {
        lights.each {
            it, _ -> it.setPowerState(false)
        }
    }

}
