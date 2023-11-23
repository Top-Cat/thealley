package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.repo.SceneRepository
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Scene(
    val sceneId: Int,
    private val deviceMapper: DeviceMapper,
    private val lights: List<SceneRepository.ScenePart>
) {

    suspend fun anyOn() =
        deviceMapper.each(lights) { bulb, _ ->
            bulb.getPowerState()
        }.any { it }

    private fun coerceToBrightness(avg: Double) =
        when {
            avg.isNaN() -> 0
            else -> min(100, max(0, avg.roundToInt()))
        }

    suspend fun averageBrightness() = coerceToBrightness(
        deviceMapper.each(lights) { bulb, _ ->
            when (bulb) {
                is Bulb -> if (bulb.getPowerState()) bulb.getLightState()?.brightness else 0
                else -> null
            }
        }.average()
    )

    suspend fun execute(percent: Int = 100, transitionTime: Int = 1000) {
        deviceMapper.each(lights) { bulb, it ->
            when (val newBrightness = (it.brightness * percent) / 100) {
                0 -> bulb.setPowerState(false)
                else -> bulb.setComplexState(newBrightness, it.hue, it.saturation, it.colorTemp, transitionTime)
            }
        }
    }

    suspend fun off() {
        deviceMapper.each(lights) { it, _ ->
            it.setPowerState(false)
        }
    }
}
