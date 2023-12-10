package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.EnableDisableGuestNetworkCommand
import uk.co.thomasc.thealley.google.command.EnableDisableNetworkProfileCommand
import uk.co.thomasc.thealley.google.command.GetGuestNetworkPasswordCommand
import uk.co.thomasc.thealley.google.command.INetworkControlCommand
import uk.co.thomasc.thealley.google.command.TestNetworkSpeedCommand
import uk.co.thomasc.thealley.google.followup.FollowUpFailure
import uk.co.thomasc.thealley.google.followup.IFollowUpHandler
import uk.co.thomasc.thealley.google.followup.NetworkDownloadSpeedMbps
import uk.co.thomasc.thealley.google.followup.NetworkSpeedMbps
import uk.co.thomasc.thealley.google.followup.NetworkUploadSpeedMbps
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

class NetworkControlTrait(
    private val attributes: NetworkControlAttributes = NetworkControlAttributes(),
    private val getNetworkState: () -> NetworkControlState,
    private val setGuestNetwork: ((Boolean) -> Unit)? = null,
    private val setNetwork: ((String, Boolean) -> Unit)? = null,
    private val getGuestPassword: () -> String
) : GoogleHomeTrait<INetworkControlCommand<*>>() {
    override val name = "action.devices.traits.NetworkControl"
    override val klazz = INetworkControlCommand::class

    @Serializable
    data class NetworkControlAttributes(
        val supportsEnablingGuestNetwork: Boolean? = false,
        val supportsDisablingGuestNetwork: Boolean? = false,
        val supportsGettingGuestNetworkPassword: Boolean? = false,
        val networkProfiles: List<String>? = null,
        val supportsEnablingNetworkProfile: Boolean? = false,
        val supportsDisablingNetworkProfile: Boolean? = false,
        val supportsNetworkDownloadSpeedTest: Boolean? = false,
        val supportsNetworkUploadSpeedTest: Boolean? = false
    )

    override suspend fun getAttributes() = alleyJson.encodeToJsonElement(attributes).jsonObject

    @Serializable
    data class NetworkControlState(
        val networkEnabled: Boolean? = null,
        val networkSettings: NetworkSettings? = null,
        val guestNetworkEnabled: Boolean? = null,
        val guestNetworkSettings: NetworkSettings? = null,
        val numConnectedDevices: Int? = null,
        val networkUsageMB: Int? = null,
        val networkUsageLimitMB: Int? = null,
        val networkUsageUnlimited: Boolean? = null,
        val lastNetworkDownloadSpeedTest: SpeedTestResult.DownloadTestResult? = null,
        val lastNetworkUploadSpeedTest: SpeedTestResult.UploadTestResult? = null,
        val networkSpeedTestInProgress: Boolean? = false,
        val networkProfilesState: Map<String, NetworkProfileState>? = null
    )

    sealed interface SpeedTestResult {
        val speedMbps: Float
        val unixTimestampSec: Int
        val status: SpeedTestStatus

        @Serializable
        data class DownloadTestResult(
            @SerialName("downloadSpeedMbps")
            override val speedMbps: Float,
            override val unixTimestampSec: Int,
            override val status: SpeedTestStatus
        ) : SpeedTestResult

        @Serializable
        data class UploadTestResult(
            @SerialName("uploadSpeedMbps")
            override val speedMbps: Float,
            override val unixTimestampSec: Int,
            override val status: SpeedTestStatus
        ) : SpeedTestResult

        enum class SpeedTestStatus {
            SUCCESS, FAILURE
        }
    }

    @Serializable
    data class NetworkProfileState(val enabled: Boolean)

    @Serializable
    data class NetworkSettings(
        val ssid: String
    )

    override suspend fun getState() = alleyJson.encodeToJsonElement(getNetworkState()).jsonObject

    override suspend fun handleCommand(cmd: INetworkControlCommand<*>, followUp: IFollowUpHandler) =
        when (cmd) {
            is EnableDisableGuestNetworkCommand -> {
                setGuestNetwork?.invoke(cmd.params.enable)
                ExecuteStatus.SUCCESS()
            }
            is EnableDisableNetworkProfileCommand -> {
                setNetwork?.invoke(cmd.params.profile, cmd.params.enable)
                ExecuteStatus.SUCCESS()
            }
            is GetGuestNetworkPasswordCommand -> {
                ExecuteStatus.SUCCESS(
                    mapOf(
                        "guestNetworkPassword" to JsonPrimitive(getGuestPassword())
                    )
                )
            }
            is TestNetworkSpeedCommand -> {
                val result = when {
                    cmd.params.testDownloadSpeed && cmd.params.testUploadSpeed -> NetworkSpeedMbps(100f, 100f)
                    cmd.params.testDownloadSpeed -> NetworkDownloadSpeedMbps(100f)
                    cmd.params.testUploadSpeed -> NetworkUploadSpeedMbps(100f)
                    else -> FollowUpFailure(GoogleHomeErrorCode.NetworkSpeedTestInProgress)
                }
                followUp.invoke(result)

                ExecuteStatus.PENDING
            }
        }
}
