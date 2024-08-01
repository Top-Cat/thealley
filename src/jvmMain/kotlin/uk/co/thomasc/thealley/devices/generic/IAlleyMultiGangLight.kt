package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventEmitter

interface IAlleyMultiGangLight : IAlleyMultiGangRelay {
    suspend fun getLightState(index: Int): IAlleyLight.LightState

    suspend fun setComplexState(bus: AlleyEventEmitter, index: Int, lightState: IAlleyLight.LightState, transitionTime: Int? = 1000)
}
