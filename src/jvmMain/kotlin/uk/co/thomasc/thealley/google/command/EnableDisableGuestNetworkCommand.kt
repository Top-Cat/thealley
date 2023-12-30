package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.EnableDisableGuestNetwork")
data class EnableDisableGuestNetworkCommand(override val params: Params) : INetworkControlCommand<EnableDisableGuestNetworkCommand.Params> {
    @Serializable
    data class Params(val enable: Boolean)
}
