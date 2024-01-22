package uk.co.thomasc.thealley.devices.system

import uk.co.thomasc.thealley.devices.AlleyDevice

data class ReportStateEvent(val deviceId: Int) : IAlleyEvent {
    constructor(device: AlleyDevice<*, *, *>) : this(device.id)
}
