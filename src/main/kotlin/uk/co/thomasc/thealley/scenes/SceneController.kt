package uk.co.thomasc.thealley.scenes

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.repo.SceneRepository
import uk.co.thomasc.thealley.repo.SwitchRepository

@Component
class SceneController(sceneRepository: SceneRepository, switchRepository: SwitchRepository) {

    val scenes = sceneRepository.getScenes()
    val rules = sceneRepository.getRules(scenes)
    val switches = switchRepository.getSwitches(scenes)

    @Scheduled(fixedDelay = 1000)
    fun tick() {
        for (rule in rules) {
            rule.tick()
        }
    }

    fun onChange(sensorId: String) {
        rules.filter { it.sensors.contains(sensorId) }.forEach { it.onChange() }
    }
}
