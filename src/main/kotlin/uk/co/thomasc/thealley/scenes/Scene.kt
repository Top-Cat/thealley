package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.client.LocalClient

data class ScenePart(val lightId: String, val brightness: Int, val hue: Int)

class Scene(private val kasa: LocalClient, private val lights: List<ScenePart>) {

    fun execute() {
        lights.map {
            kasa.getDevice("lb130-${it.lightId}.guest.kirkstall.top-cat.me").bulb { bulb ->
                when (it.brightness) {
                    0 -> bulb?.setPowerState(false)
                    else -> bulb?.setComplexState(it.brightness, it.hue)
                }
            }
        }
    }

}
