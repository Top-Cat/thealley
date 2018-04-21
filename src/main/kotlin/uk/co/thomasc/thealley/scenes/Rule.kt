package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class Rule(
    private val sceneRepository: SceneRepository,

    val id: Int,
    val sensorId: String,
    val state: Boolean,
    var lastActive: LocalDateTime?,
    private val scene: Scene
) {

    private val timeout = 5 * 60

    fun onChange() {
        if (state) {
            scene.execute()
        } else {
            lastActive = LocalDateTime.now()
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

    fun tick() {
        val difference = Duration.between(LocalDateTime.now(), lastActive)
        val secondsSinceActivity = difference.seconds.absoluteValue

        if (lastActive != null && secondsSinceActivity > timeout) {
            scene.execute()

            lastActive = null
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

}
