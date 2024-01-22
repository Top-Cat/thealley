package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

interface AlleyEventEmitter {
    suspend fun <T : IAlleyEvent> emit(event: T)
}
