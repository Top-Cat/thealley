package uk.co.thomasc.thealley.devices.system.conditional.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.wiim.TransportState

@Serializable
@SerialName("Wiim")
data class WiimCondition(
    val uuid: String,
    val state: TransportState
) : ICondition
