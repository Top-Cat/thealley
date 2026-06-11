package uk.co.thomasc.thealley.devices.zigbee.sonoff

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class WaterLeakEvent(
    val deviceId: Int
) : IAlleyEvent
