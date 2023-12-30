package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mediaShuffle")
data class MediaShuffleCommand(override val params: NoParams) : ITransportControlCommand<NoParams>
