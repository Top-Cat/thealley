package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeHumidity
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTANew
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeTemperature
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindCommand
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class BlindUpdate(
    override val battery: Float?,
    val voltage: Int,
    override val temperature: Float,
    override val humidity: Float?,
    override val linkquality: Int,
    val position: Int,
    val velocityLift: Int,
    val setup: ZRelayAction,
    val state: BlindCommand,
    override val update: ZigbeeOTAStatus
) : ZigbeeUpdateBattery, ZigbeeTemperature, ZigbeeHumidity, ZigbeeOTANew



