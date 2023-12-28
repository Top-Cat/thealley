package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeePowerMonitoring
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.moes.ZigbeePowerOnBehavior
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class SDimmerUpdate(
    override val linkquality: Int,
    override val voltage: Float,
    override val current: Float,
    override val energy: Float,
    override val power: Float,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior,

    val brightness: Int,
    override val state: ZRelayAction
) : ZigbeeUpdateMains, ZigbeePowerMonitoring, ZigbeeUpdateRelay
