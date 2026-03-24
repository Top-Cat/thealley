package uk.co.thomasc.thealley.devices.zigbee.blind

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTANew
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery

@Serializable
data class BlindMotorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Float? = null,

    // Blind motor
    @SerialName("motor_state")
    val motorState: BlindMotorState? = null,
    val position: Int? = null,
    @SerialName("power_outage_count")
    val powerOutageCount: Int? = null,
    val running: Boolean? = null,
    val state: BlindUpdateState? = null,
    override val update: ZigbeeOTAStatus
) : ZigbeeUpdateBattery, ZigbeeOTANew
