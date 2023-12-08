package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.GetGuestNetworkPassword")
data class GetGuestNetworkPasswordCommand(override val params: NoParams) : INetworkControlCommand<NoParams>
