package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IAlleyMultiGangLight : IAlleyMultiGangRelay {
    suspend fun getLightState(index: Int): IAlleyLight.LightState

    suspend fun setComplexState(bus: AlleyEventBus, index: Int, lightState: IAlleyLight.LightState, transitionTime: Int? = 1000)
}
