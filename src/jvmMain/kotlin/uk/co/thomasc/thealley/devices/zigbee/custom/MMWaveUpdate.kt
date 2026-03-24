package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTANew
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeTemperature
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class MMWaveUpdate(
    val bluetooth: ZRelayAction,
    val illuminance: Float,
    val occupancy: Boolean,
    @SerialName("occupancy_timeout")
    val occupancyTimeout: Int,
    override val temperature: Float,
    override val linkquality: Int,
    override val update: ZigbeeOTAStatus
) : ZigbeeTemperature, ZigbeeOTANew
