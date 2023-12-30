package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.NextInput")
data class NextInputCommand(override val params: NoParams) : IInputSelectorCommand<NoParams>
