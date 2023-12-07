package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mediaRepeatMode")
data class MediaRepeatModeCommand(override val params: Params) : ITransportControlCommand<MediaRepeatModeCommand.Params> {
    @Serializable
    data class Params(val isOn: Boolean, val isSingle: Boolean = false)
}
