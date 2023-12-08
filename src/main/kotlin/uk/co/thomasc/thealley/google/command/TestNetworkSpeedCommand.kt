package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.TestNetworkSpeed")
data class TestNetworkSpeedCommand(override val params: Params) : INetworkControlCommand<TestNetworkSpeedCommand.Params> {
    @Serializable
    data class Params(val testDownloadSpeed: Boolean, val testUploadSpeed: Boolean, val followUpToken: String)
}
