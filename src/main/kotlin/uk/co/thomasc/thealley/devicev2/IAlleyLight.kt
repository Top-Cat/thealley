package uk.co.thomasc.thealley.devicev2

interface IAlleyLight : IAlleyRevocable {
    suspend fun setPowerState(bus: AlleyEventBus, value: Boolean)

    suspend fun getLightState(): LightState

    suspend fun setComplexState(bus: AlleyEventBus, lightState: LightState, transitionTime: Int? = 1000)

    suspend fun getPowerState(): Boolean

    suspend fun togglePowerState(bus: AlleyEventBus)

    data class LightState(val brightness: Int? = null, val hue: Int? = null, val saturation: Int? = null, val temperature: Int? = null)
}

interface IAlleyRevocable {
    suspend fun revoke()
}
