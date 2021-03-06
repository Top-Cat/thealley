package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.repo.SwitchRepository
import uk.co.thomasc.thealley.scenes.Scene
import kotlin.math.max
import kotlin.math.min

const val FADE_TIME = 5 * 10

data class Switch(
    val switchId: Int,
    val buttonId: Int,
    var state: Int,
    private val scene: Scene,
    private val switchRepository: SwitchRepository
) {
    private var fadeStarted: Long = 0
    private var startBrightness = 0

    fun toggle() {
        if (scene.anyOn()) {
            state = 0
            scene.off()
        } else {
            state = 100
            scene.execute()
        }

        switchRepository.updateSwitchState(this)
    }

    fun revoke() {
        //TODO: revoke override so rules can change light state
    }

    fun startFade() {
        fadeStarted = System.currentTimeMillis()

        startBrightness = scene.averageBrightness()
        scene.execute(startBrightness, 0)
        if (state > 0) {
            // Fade to 0
            scene.execute(0, startBrightness * FADE_TIME)
        } else {
            // Fade to 100
            scene.execute(100, (100 - startBrightness) * FADE_TIME)
        }
    }

    fun endFade() {
        val fadeTime = System.currentTimeMillis() - fadeStarted

        val newBrightness = if (state > 0) {
            state = 0
            max(((startBrightness * FADE_TIME) - fadeTime) / FADE_TIME, 1)
        } else {
            state = 100
            min(((startBrightness * FADE_TIME) + fadeTime) / FADE_TIME, 100)
        }.toInt()

        scene.execute(newBrightness, 0)
        switchRepository.updateSwitchState(this)
    }
}
