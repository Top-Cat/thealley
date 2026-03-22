package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdate
import uk.co.thomasc.thealley.devices.zigbee.blind.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class MMWaveUpdate(
    val bluetooth: ZRelayAction,
    val illuminance: Float,
    val occupancy: Boolean,
    @SerialName("occupancy_timeout")
    val occupancyTimeout: Int,
    val temperature: Float,
    override val linkquality: Int,
    val update: ZigbeeOTAStatus
) : ZigbeeUpdate
