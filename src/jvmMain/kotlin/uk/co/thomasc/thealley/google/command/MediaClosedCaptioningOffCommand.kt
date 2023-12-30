package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mediaClosedCaptioningOff")
data class MediaClosedCaptioningOffCommand(override val params: NoParams) : ITransportControlCommand<NoParams>
