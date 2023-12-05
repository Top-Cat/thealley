package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.BrightnessRelative")
data class BrightnessRelativeCommand(override val params: Params) : IBrightnessCommand<BrightnessRelativeCommand.Params> {
    @Serializable
    data class Params(
        val brightnessRelativePercent: Int? = null,
        val brightnessRelativeWeight: Int? = null
    )
}
