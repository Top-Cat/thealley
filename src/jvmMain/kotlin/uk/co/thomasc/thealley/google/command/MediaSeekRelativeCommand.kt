package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mediaSeekRelative")
data class MediaSeekRelativeCommand(override val params: Params) : ITransportControlCommand<MediaSeekRelativeCommand.Params> {
    @Serializable
    data class Params(val relativePositionMs: Int)
}
