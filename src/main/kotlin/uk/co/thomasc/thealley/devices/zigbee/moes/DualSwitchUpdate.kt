package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class DualSwitchUpdate(
    override val linkquality: Int,
    override val voltage: Float = 0f,

    @SerialName("state_l1")
    val state1: ZRelayAction,
    @SerialName("state_l2")
    val state2: ZRelayAction,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior
) : ZigbeeUpdateMains
