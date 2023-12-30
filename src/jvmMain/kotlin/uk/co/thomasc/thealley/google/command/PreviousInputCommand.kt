package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.PreviousInput")
data class PreviousInputCommand(override val params: NoParams) : IInputSelectorCommand<NoParams>
