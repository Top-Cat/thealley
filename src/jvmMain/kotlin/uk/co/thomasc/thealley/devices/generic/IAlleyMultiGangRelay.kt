package uk.co.thomasc.thealley.devices.generic

import uk.co.thomasc.thealley.devices.AlleyEventEmitter

interface IAlleyMultiGangRelay {
    suspend fun setPowerState(bus: AlleyEventEmitter, index: Int, value: Boolean)
    suspend fun getPowerState(index: Int): Boolean
    suspend fun togglePowerState(bus: AlleyEventEmitter, index: Int)
}
