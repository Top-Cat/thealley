package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class Rule(
    private val sceneRepository: SceneRepository,

    private val id: Int,
    val sensorId: String,
    private val timeout: Int,
    var lastActive: LocalDateTime?,
    private val scene: Scene
) {

    fun onChange() {
        if (timeout == 0) {
            scene.execute()
        } else {
            lastActive = LocalDateTime.now()
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

    fun tick() {
        if (lastActive == null) return

        val difference = Duration.between(LocalDateTime.now(), lastActive)
        val secondsSinceActivity = difference.seconds.absoluteValue

        if (secondsSinceActivity > timeout) {
            scene.execute()

            lastActive = null
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

}
