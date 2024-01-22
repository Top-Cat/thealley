package uk.co.thomasc.thealley.devices.zigbee.candeo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAInfo
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdate
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateOTA
import uk.co.thomasc.thealley.devices.zigbee.moes.ZigbeePowerOnBehavior
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateDimmer

@Serializable
data class CDimmerUpdate(
    override val linkquality: Int,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior,

    @SerialName("update_available")
    override val updateAvailable: Boolean? = null,
    override val update: ZigbeeOTAInfo? = null,

    override val brightness: Int,
    override val state: ZRelayAction
) : ZigbeeUpdate, ZigbeeUpdateOTA, ZigbeeUpdateDimmer
