package uk.co.thomasc.thealley.scenes

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.repo.SceneRepository

@Component
class SceneController(sceneRepository: SceneRepository) {

    val rules: List<Rule> = sceneRepository.getRules()

    @Scheduled(fixedDelay = 1000)
    fun tick() {
        for (rule in rules) {
            rule.tick()
        }
    }

    fun onChange(sensorId: String) {
        rules.filter { it.sensorId == sensorId }.forEach { it.onChange() }
    }
}
