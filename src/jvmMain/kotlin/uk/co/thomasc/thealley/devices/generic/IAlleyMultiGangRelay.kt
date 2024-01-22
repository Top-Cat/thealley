package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventBus

interface IAlleyMultiGangRelay {
    suspend fun setPowerState(bus: AlleyEventBus, index: Int, value: Boolean)
    suspend fun getPowerState(index: Int): Boolean
    suspend fun togglePowerState(bus: AlleyEventBus, index: Int)
}
