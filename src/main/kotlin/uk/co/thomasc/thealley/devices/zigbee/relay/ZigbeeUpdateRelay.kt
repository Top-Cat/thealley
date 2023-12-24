package uk.co.thomasc.thealley.devices.zigbee.relay

import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdate

interface ZigbeeUpdateRelay : ZigbeeUpdate {
    val state: ZRelayAction
}
