package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.google.GoogleHomeLang

@Serializable
@SerialName("action.devices.commands.mediaClosedCaptioningOn")
data class MediaClosedCaptioningOnCommand(override val params: Params) : ITransportControlCommand<MediaClosedCaptioningOnCommand.Params> {
    @Serializable
    data class Params(
        val closedCaptioningLanguage: GoogleHomeLang? = null,
        val userQueryLanguage: GoogleHomeLang? = null
    )
}
