package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.devices.DeviceMapper
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class ScenePart(val sceneId: Int, override val deviceId: Int, val brightness: Int, val hue: Int?, val saturation: Int?, val colorTemp: Int?) : DeviceMapper.HasDeviceId

class Scene(
    val sceneId: Int,
    private val deviceMapper: DeviceMapper,
    private val lights: List<ScenePart>
) {

    fun anyOn() =
        deviceMapper.each(lights) {
            bulb, _ -> bulb.getPowerState()
        }.any { it }

    private fun coerceToBrightness(avg: Double) =
        when {
            avg.isNaN() -> 0
            else -> min(100, max(0, avg.roundToInt()))
        }

    fun averageBrightness() = coerceToBrightness(
        deviceMapper.each(lights) {
            bulb, _ ->
            when (bulb) {
                is Bulb -> if (bulb.getPowerState()) bulb.getLightState().brightness else 0
                else -> null
            }
        }.average())

    fun execute(percent: Int = 100, transitionTime: Int = 1000) {
        deviceMapper.each(lights) {
            bulb, it ->

            when (it.brightness) {
                0 -> bulb.setPowerState(false)
                else -> bulb.setComplexState((it.brightness * percent) / 100, it.hue, it.saturation, it.colorTemp, transitionTime)
            }
        }
    }

    fun off() {
        deviceMapper.each(lights) {
            it, _ -> it.setPowerState(false)
        }
    }

}
