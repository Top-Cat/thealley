package uk.co.thomasc.thealley.devices.zigbee.zbmini

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class ZBMiniUpdate(
    override val linkquality: Int,

    override val voltage: Float = 0f,

    override val state: ZRelayAction
) : ZigbeeUpdateMains, ZigbeeUpdateRelay
