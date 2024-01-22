package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IAlleyRelay {
    suspend fun setPowerState(bus: AlleyEventBus, value: Boolean)
    suspend fun getPowerState(): Boolean
    suspend fun togglePowerState(bus: AlleyEventBus)
}
