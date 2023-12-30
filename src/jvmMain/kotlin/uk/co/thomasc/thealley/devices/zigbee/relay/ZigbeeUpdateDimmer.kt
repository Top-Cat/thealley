package uk.co.thomasc.thealley.devices.zigbee.relay

interface ZigbeeUpdateDimmer : ZigbeeUpdateRelay {
    val brightness: Int
}
