package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventEmitter

interface IAlleyLight : IAlleyRelay {
    suspend fun getLightState(): LightState

    suspend fun setComplexState(bus: AlleyEventEmitter, lightState: LightState, transitionTime: Int? = 1000)

    data class LightState(override val brightness: Int? = null, override val hue: Int? = null, override val saturation: Int? = null, override val temperature: Int? = null) : ILightState {
        fun brightness255() = ((brightness ?: 0) * 2.54f).toInt()
    }
}
