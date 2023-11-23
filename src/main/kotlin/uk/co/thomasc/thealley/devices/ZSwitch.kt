package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uk.co.thomasc.thealley.client.ZigbeeUpdate
import uk.co.thomasc.thealley.client.alleyJson
import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.Scene
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class BrightnessUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Int?,

    // Blind motor
    val action: String,
    val action_step_size: Int,
    val action_transition_time: Float
) : ZigbeeUpdate

class ZSwitch(val scene: Scene, val obj: SwitchRepository.ZSwitchObj) {
    var lastKnownState = 0

    companion object {
        val threadPool = newFixedThreadPoolContext(4, "ZSwitch")
    }

    fun handleMessage(node: JsonElement) {
        GlobalScope.launch(threadPool) {
            val state = scene.averageBrightness()

            when (node.jsonObject["action"]?.jsonPrimitive?.content) {
                "off", "on" -> {
                    if (state == 0) {
                        scene.execute(lastKnownState)
                    } else {
                        lastKnownState = state
                        scene.off()
                    }
                }
                "brightness_step_up", "brightness_step_down", "color_temperature_step_up", "color_temperature_step_down" -> {
                    if (state == 0) return@launch
                    val toTheRight = setOf("brightness_step_up", "color_temperature_step_down")

                    val update = alleyJson.decodeFromJsonElement<BrightnessUpdate>(node)
                    val stepSize = (update.action_step_size / 2.55f).roundToInt()

                    val step = if (toTheRight.contains(update.action)) stepSize else -stepSize
                    lastKnownState = min(100, max(1, state + step))
                    scene.execute(lastKnownState, (update.action_transition_time * 1000).roundToInt())
                }
            }
        }
    }
}
