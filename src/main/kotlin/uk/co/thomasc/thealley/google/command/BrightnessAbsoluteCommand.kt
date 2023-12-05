package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.BrightnessAbsolute")
data class BrightnessAbsoluteCommand(override val params: Params) : IBrightnessCommand<BrightnessAbsoluteCommand.Params> {
    @Serializable
    data class Params(val brightness: Int)
}
