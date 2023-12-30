package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.OnOff")
data class OnOffCommand(override val params: Params) : IGoogleHomeCommand<OnOffCommand.Params> {
    @Serializable
    data class Params(val on: Boolean)
}
