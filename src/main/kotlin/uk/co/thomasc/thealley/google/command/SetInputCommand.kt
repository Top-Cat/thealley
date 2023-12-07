package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.SetInput")
data class SetInputCommand(override val params: Params) : IInputSelectorCommand<SetInputCommand.Params> {
    @Serializable
    data class Params(val newInput: String)
}
