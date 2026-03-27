package uk.co.thomasc.thealley.devices.zigbee.custom

import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class LowBatteryEvent(
    val deviceId: Int,
    val batteryLevel: Float
) : IAlleyEvent
