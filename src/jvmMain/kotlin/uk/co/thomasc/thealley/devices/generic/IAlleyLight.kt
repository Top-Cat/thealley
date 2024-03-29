package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IAlleyLight : IAlleyRelay {
    suspend fun getLightState(): LightState

    suspend fun setComplexState(bus: AlleyEventBus, lightState: LightState, transitionTime: Int? = 1000)

    data class LightState(override val brightness: Int? = null, override val hue: Int? = null, override val saturation: Int? = null, override val temperature: Int? = null) : ILightState
}
