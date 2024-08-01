package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventEmitter

interface IAlleyRelay {
    suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean)
    suspend fun getPowerState(): Boolean
    suspend fun togglePowerState(bus: AlleyEventEmitter)
}
