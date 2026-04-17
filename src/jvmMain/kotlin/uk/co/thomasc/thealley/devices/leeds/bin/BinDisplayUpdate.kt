package uk.co.thomasc.thealley.devices.leeds.bin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTANew
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAStatus
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery

@Serializable
data class BinDisplayUpdate(
    override val battery: Float?,
    @SerialName("display_times")
    val displayTimes: BinDisplayTimes,
    val voltage: Int,
    override val linkquality: Int,
    override val update: ZigbeeOTAStatus
) : ZigbeeUpdateBattery, ZigbeeOTANew

@Serializable
data class TimeUpdateCommand(
    @SerialName("display_times")
    val displayTimes: BinDisplayTimes
)

@Serializable
data class BinDisplayTimes(
    val black: Long,
    val green: Long,
    val brown: Long
)
