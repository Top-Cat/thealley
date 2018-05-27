package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.devices.DeviceMapper

data class ScenePart(val sceneId: Int, override val deviceId: Int, val brightness: Int, val hue: Int?, val saturation: Int?, val colorTemp: Int?) : DeviceMapper.HasDeviceId

class Scene(
    val sceneId: Int,
    private val deviceMapper: DeviceMapper,
    private val lights: List<ScenePart>
) {

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
