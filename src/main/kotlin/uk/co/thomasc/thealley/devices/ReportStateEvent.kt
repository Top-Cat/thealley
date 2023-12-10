package uk.co.thomasc.thealley.devices

data class ReportStateEvent(val deviceId: Int) : IAlleyEvent {
    constructor(device: AlleyDevice<*, *, *>) : this(device.id)
}
