package uk.co.thomasc.thealley.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.scenes.SceneController

sealed class EventData(open val sensor: String)
data class MotionData(override val sensor: String) : EventData(sensor)
data class PropertyData(override val sensor: String, val property: String, val value: Double) : EventData(sensor)

@RestController
@RequestMapping("/api")
class Api(val sceneController: SceneController) {
    val sensorData = mutableMapOf<String, MutableMap<String, Double>>()

    @PostMapping("/motion")
    fun onMotion(@RequestBody obj: MotionData) {
        sceneController.onChange(obj.sensor)
    }

    @PostMapping("/prop")
    fun onPropertyChange(@RequestBody obj: PropertyData) {
        sensorData.getOrElse(obj.sensor) {
            val newObj = mutableMapOf<String, Double>()
            sensorData[obj.sensor] = newObj
            newObj
        }.let {
            it[obj.property] = obj.value
        }
    }

    @GetMapping("/prop")
    fun sensorData() = sensorData
}
