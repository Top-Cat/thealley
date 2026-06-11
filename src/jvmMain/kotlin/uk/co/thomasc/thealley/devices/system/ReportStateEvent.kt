package uk.co.thomasc.thealley.devices.system

import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.google.followup.IFollowUpNotification

data class ReportStateEvent(val deviceId: Int, val notifications: Map<String, IFollowUpNotification>? = null) : IAlleyEvent {
    constructor(device: AlleyDevice<*, *, *>, notifications: Map<String, IFollowUpNotification>? = null) : this(device.id, notifications)
}
