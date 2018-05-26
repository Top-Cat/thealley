package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SwitchRepository

data class ScenePart(val sceneId: Int, val lightId: Int, val brightness: Int, val hue: Int?, val saturation: Int?, val colorTemp: Int?)

class Scene(
    val sceneId: Int,
    localClient: LocalClient,
    relayClient: RelayClient,
    switchRepository: SwitchRepository,
    private val lights: List<ScenePart>
) : DeviceMapper(localClient, relayClient, switchRepository) {

    fun execute(percent: Int = 100, transitionTime: Int = 1000) {
        lights.each {
            bulb, it ->

            when (it.brightness) {
                0 -> bulb.setPowerState(false)
                else -> bulb.setComplexState((it.brightness * percent) / 100, it.hue, it.saturation, it.colorTemp, transitionTime)
            }
        }
    }

    fun off() {
        lights.each {
            it, _ -> it.setPowerState(false)
        }
    }

}
