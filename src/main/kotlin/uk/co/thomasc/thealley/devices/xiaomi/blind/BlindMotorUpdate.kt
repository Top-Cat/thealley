package uk.co.thomasc.thealley.devices.xiaomi.blind

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.xiaomi.ZigbeeUpdate

@Serializable
data class BlindMotorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Int? = null,
    @SerialName("device_temperature")
    override val deviceTemperature: Int? = null,

    // Blind motor
    @SerialName("motor_state")
    val motorState: BlindMotorState? = null,
    val position: Int? = null,
    @SerialName("power_outage_count")
    val powerOutageCount: Int? = null,
    val running: Boolean? = null,
    val state: BlindUpdateState? = null,
    val update: ZigbeeOTAStatus
) : ZigbeeUpdate

@Serializable
data class ZigbeeOTAStatus(
    @SerialName("installed_version")
    val installedVersion: Int,
    @SerialName("latest_version")
    val latestVersion: Int,
    val state: ZigbeeOTAState
)

enum class ZigbeeOTAState {
    @SerialName("idle")
    IDLE
}
