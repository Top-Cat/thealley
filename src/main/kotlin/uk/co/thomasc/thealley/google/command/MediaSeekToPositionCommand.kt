package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mediaSeekToPosition")
data class MediaSeekToPositionCommand(override val params: Params) : ITransportControlCommand<MediaSeekToPositionCommand.Params> {
    @Serializable
    data class Params(val absPositionMs: Int)
}
