package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
fun interface EventHandler<in T : IAlleyEvent> {
    suspend fun invoke(event: T)
}
