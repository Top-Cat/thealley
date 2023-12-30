package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait

@Serializable
@SerialName("action.devices.commands.OpenClose")
data class OpenCloseCommand(override val params: Params) : IOpenCloseCommand<OpenCloseCommand.Params> {
    @Serializable
    data class Params(
        val openPercent: Int,
        val openDirection: OpenCloseTrait.Direction? = null,
        val followUpToken: String? = null
    )
}
