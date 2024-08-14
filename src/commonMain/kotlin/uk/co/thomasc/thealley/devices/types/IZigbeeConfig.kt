package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState

interface IZigbeeConfig<U : IZigbeeState> : IAlleyConfig<U> {
    val prefix: String
    val deviceId: String
}
