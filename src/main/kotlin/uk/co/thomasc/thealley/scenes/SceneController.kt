package uk.co.thomasc.thealley.scenes

import uk.co.thomasc.thealley.repo.SceneRepository
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SceneController(sceneRepository: SceneRepository, switchRepository: SwitchRepository) {
    private val executor = Executors.newScheduledThreadPool(1)

    init {
        executor.scheduleWithFixedDelay(::tick, 1L, 1L, TimeUnit.SECONDS)
    }

    val scenes = sceneRepository.getScenes()
    val rules = sceneRepository.getRules(scenes)
    private val switchDelegate = resetableLazy {
        switchRepository.getSwitches(scenes)
    }
    val switches by switchDelegate

    private fun tick() {
        for (rule in rules) {
            rule.tick()
        }
    }

    fun onChange(sensorId: String) {
        rules.filter { it.sensors.contains(sensorId) }.forEach { it.onChange() }
    }

    fun resetSwitchDelegate() {
        switchDelegate.reset()
    }
}
