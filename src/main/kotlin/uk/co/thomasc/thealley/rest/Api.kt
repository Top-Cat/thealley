package uk.co.thomasc.thealley.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.co.thomasc.thealley.scenes.SceneController

sealed class EventData(open val id: String)
data class MotionData(override val id: String) : EventData(id)
data class PropertyData(override val id: String, val prop: String, val value: Double) : EventData(id)

@RestController
@RequestMapping("/api")
class Api(val sceneController: SceneController) {
    @PostMapping("/motion")
    fun onMotion(@RequestBody obj: MotionData) {
        sceneController.onChange(obj.id)
    }

    @PostMapping("/prop")
    fun onPropertyChange(@RequestBody obj: PropertyData) = ResponseEntity.ok().build<Void>()
}
