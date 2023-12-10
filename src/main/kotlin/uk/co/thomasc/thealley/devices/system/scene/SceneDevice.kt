package uk.co.thomasc.thealley.devices.system.scene

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IAlleyRevocable
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.types.SceneConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.SceneTrait
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SceneDevice(id: Int, config: SceneConfig, state: SceneState, stateStore: IStateUpdater<SceneState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<SceneDevice, SceneConfig, SceneState>(id, config, state, stateStore) {

    private suspend fun getLights() = config.parts.map { dev.getDevice(it.lightId) }.filterIsInstance<IAlleyLight>()
    private suspend fun anyOn() = getLights().any { it.getPowerState() }
    private suspend fun averageBrightness() = coerceToBrightness(
        getLights()
            .map {
                if (it.getPowerState()) { it.getLightState().brightness ?: 0 } else { 0 }
            }.average()
    )
    private fun coerceToBrightness(avg: Double) =
        when {
            avg.isNaN() -> 0
            else -> min(100, max(0, avg.roundToInt()))
        }

    private suspend fun emitFadeEvent(bus: AlleyEventBus, s: SceneConfig.ScenePart, percent: Int, transitionTime: Int = 0) {
        val light = dev.getDevice(s.lightId)
        if (light is IAlleyLight) {
            light.setComplexState(bus, IAlleyLight.LightState((s.brightness * percent) / 100, s.hue, s.saturation, s.temperature), transitionTime)
        }
        if (light is IAlleyRevocable) {
            light.hold()
        }
    }

    suspend fun off(bus: AlleyEventBus) {
        config.parts.forEach { s ->
            val light = dev.getDevice(s.lightId)
            if (light is IAlleyLight) {
                light.setPowerState(bus, false)
            }
            if (light is IAlleyRevocable) {
                light.hold()
            }
        }
    }

    suspend fun execute(bus: AlleyEventBus) {
        config.parts.forEach { s ->
            emitFadeEvent(bus, s, 100, 1000)
        }
    }

    suspend fun toggle(bus: AlleyEventBus) {
        if (anyOn()) {
            off(bus)
        } else {
            execute(bus)
        }
    }

    override suspend fun init(bus: AlleyEventBus) {
        registerGoogleHomeDevice(
            DeviceType.SCENE,
            false,
            SceneTrait(
                executeScene = {
                    execute(bus)
                }
            )
        )

        bus.handle<SceneEvent> { ev ->
            if (ev.scene != id) return@handle

            when (ev.action) {
                SceneEvent.Action.TOGGLE -> toggle(bus)
                SceneEvent.Action.REVOKE -> {
                    config.parts.forEach { s ->
                        val light = dev.getDevice(s.lightId)
                        if (light is IAlleyRevocable) light.revoke()
                    }
                }
                SceneEvent.Action.START_FADE -> {
                    updateState(state.copy(fadeStarted = System.currentTimeMillis(), startBrightness = averageBrightness()))
                    config.parts.forEach { s ->
                        emitFadeEvent(bus, s, state.startBrightness)
                        if (state.direction) {
                            // Fade to 0
                            emitFadeEvent(bus, s, 0, state.startBrightness * FADE_TIME)
                        } else {
                            // Fade to 100
                            emitFadeEvent(bus, s, 100, (100 - state.startBrightness) * FADE_TIME)
                        }
                    }
                }
                SceneEvent.Action.END_FADE -> {
                    val fadeTime = System.currentTimeMillis() - state.fadeStarted

                    val newBrightness = if (state.direction) {
                        max(((state.startBrightness * FADE_TIME) - fadeTime) / FADE_TIME, 1)
                    } else {
                        min(((state.startBrightness * FADE_TIME) + fadeTime) / FADE_TIME, 100)
                    }.toInt()

                    config.parts.forEach { s ->
                        emitFadeEvent(bus, s, newBrightness)
                    }
                    updateState(state.copy(direction = !state.direction))
                }
            }
        }
    }

    companion object : KLogging() {
        const val FADE_TIME = 5 * 10
    }
}
