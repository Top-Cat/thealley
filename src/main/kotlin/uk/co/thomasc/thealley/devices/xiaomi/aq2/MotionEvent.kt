package uk.co.thomasc.thealley.devices.xiaomi.aq2

import uk.co.thomasc.thealley.devices.IAlleyEvent

data class MotionEvent(val id: Int, val deviceId: String) : IAlleyEvent
