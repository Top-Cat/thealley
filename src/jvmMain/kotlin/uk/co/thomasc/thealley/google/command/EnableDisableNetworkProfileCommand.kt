package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.EnableDisableNetworkProfile")
data class EnableDisableNetworkProfileCommand(override val params: Params) : INetworkControlCommand<EnableDisableNetworkProfileCommand.Params> {
    @Serializable
    data class Params(val profile: String, val enable: Boolean)
}
