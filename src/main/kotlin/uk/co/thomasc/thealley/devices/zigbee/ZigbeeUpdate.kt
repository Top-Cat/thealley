package uk.co.thomasc.thealley.devices.zigbee

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ZigbeeUpdate {
    val linkquality: Int
    val deviceTemperature: Int?
}

interface ZigbeeUpdateBattery : ZigbeeUpdate {
    val battery: Int?
}

interface ZigbeeUpdateMains : ZigbeeUpdate {
    val voltage: Float
}

interface ZigbeePowerMonitoring {
    val current: Float
    val energy: Float
    val power: Float
}

interface ZigbeeUpdateOTA {
    val updateAvailable: Boolean?
    val update: ZigbeeOTAInfo?
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
