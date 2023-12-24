package uk.co.thomasc.thealley.devices.zigbee.zbmini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class ZBMiniUpdate(
    override val linkquality: Int,
    @SerialName("device_temperature")
    override val deviceTemperature: Int? = null,

    override val voltage: Float = 0f,

    override val state: ZRelayAction
) : ZigbeeUpdateMains, ZigbeeUpdateRelay
