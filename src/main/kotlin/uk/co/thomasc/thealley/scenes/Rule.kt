package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class Rule(
    private val sceneRepository: SceneRepository,

    private val id: Int,
    val sensors: List<String>,
    private val timeout: Int,
    var lastActive: LocalDateTime?,
    private val scene: Scene
) {

    fun onChange() {
        scene.execute()

        if (timeout > 0) {
            lastActive = LocalDateTime.now()
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

    fun tick() {
        if (lastActive == null) return

        val difference = Duration.between(LocalDateTime.now(), lastActive)
        val secondsSinceActivity = difference.seconds.absoluteValue

        if (secondsSinceActivity > timeout) {
            scene.off()

            lastActive = null
            sceneRepository.updateLastActive(id, lastActive)
        }
    }

}
