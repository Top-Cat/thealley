package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.volumeRelative")
data class VolumeRelativeCommand(override val params: Params) : IVolumeCommand<VolumeRelativeCommand.Params> {
    @Serializable
    data class Params(val relativeSteps: Int)
}
