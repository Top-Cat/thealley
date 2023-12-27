package uk.co.thomasc.thealley.devices.zigbee.relay

import uk.co.thomasc.thealley.devices.IAlleyEvent

data class MultiGangUpdate(val device: Int) : IAlleyEvent
