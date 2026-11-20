package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

fun interface EventHandler<in T : IAlleyEvent> {
    suspend fun invoke(event: T)
}
