package uk.co.thomasc.thealley.devices

interface IAlleyLight : IAlleyRelay {
    suspend fun getLightState(): LightState

    suspend fun setComplexState(bus: AlleyEventBus, lightState: LightState, transitionTime: Int? = 1000)

    data class LightState(override val brightness: Int? = null, override val hue: Int? = null, override val saturation: Int? = null, override val temperature: Int? = null) : ILightState
}

interface IAlleyMultiGangLight : IAlleyMultiGangRelay {
    suspend fun getLightState(index: Int): IAlleyLight.LightState

    suspend fun setComplexState(bus: AlleyEventBus, index: Int, lightState: IAlleyLight.LightState, transitionTime: Int? = 1000)
}

interface IAlleyRelay {
    suspend fun setPowerState(bus: AlleyEventBus, value: Boolean)
    suspend fun getPowerState(): Boolean
    suspend fun togglePowerState(bus: AlleyEventBus)
}

interface IAlleyMultiGangRelay {
    suspend fun setPowerState(bus: AlleyEventBus, index: Int, value: Boolean)
    suspend fun getPowerState(index: Int): Boolean
    suspend fun togglePowerState(bus: AlleyEventBus, index: Int)
}

interface ILightState {
    val hue: Int?
    val saturation: Int?
    val brightness: Int?
    val temperature: Int?
}

interface IAlleyRevocable {
    suspend fun hold()
    suspend fun revoke()
}