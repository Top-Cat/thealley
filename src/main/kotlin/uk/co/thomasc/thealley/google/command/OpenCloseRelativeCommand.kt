package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

@Serializable
@SerialName("action.devices.commands.OpenCloseRelative")
data class OpenCloseRelativeCommand(override val params: Params) : IOpenCloseCommand<OpenCloseRelativeCommand.Params> {
    @Serializable
    data class Params(
        val openRelativePercent: Int,
        val openDirection: OpenCloseTrait.Direction? = null
    )
}
