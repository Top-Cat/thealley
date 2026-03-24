package uk.co.thomasc.thealley.devices.zigbee

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ZigbeeUpdate {
    val linkquality: Int
}

interface ZigbeeTemperature : ZigbeeUpdate {
    val temperature: Float?
}

interface ZigbeeHumidity : ZigbeeUpdate {
    val humidity: Float?
}

interface ZigbeeUpdateBattery : ZigbeeUpdate {
    val battery: Float?
}

interface ZigbeeUpdateMains : ZigbeeUpdate {
    val voltage: Float
}

interface ZigbeePowerMonitoring : ZigbeeUpdate {
    val current: Float
    val energy: Float
    val power: Float
}

interface ZigbeeUpdateOTA : ZigbeeUpdate {
    val updateAvailable: Boolean?
    val update: ZigbeeOTAInfo?
}

interface ZigbeeOTANew : ZigbeeUpdate {
    val update: ZigbeeOTAStatus
}

@Serializable
data class ZigbeeOTAInfo(
    @SerialName("installed_version")
    val installedVersion: Int,
    @SerialName("latest_version")
    val latestVersion: Int,
    val state: String?,
    val progress: Float? = null,
    val remaining: Int? = null
)

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
    IDLE,

    @SerialName("updating")
    UPDATING,

    @SerialName("available")
    AVAILABLE,

    @SerialName("scheduled")
    SCHEDULED
}
