package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAInfo
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateOTA
import uk.co.thomasc.thealley.devices.zigbee.moes.ZigbeePowerOnBehavior
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class CDimmerUpdate(
    override val linkquality: Int,
    override val voltage: Float,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior,

    @SerialName("update_available")
    override val updateAvailable: Boolean? = null,
    override val update: ZigbeeOTAInfo? = null,

    val brightness: Int,
    override val state: ZRelayAction
) : ZigbeeUpdateMains, ZigbeeUpdateOTA, ZigbeeUpdateRelay
