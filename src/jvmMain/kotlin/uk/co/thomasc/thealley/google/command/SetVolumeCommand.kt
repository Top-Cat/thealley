package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.setVolume")
data class SetVolumeCommand(override val params: Params) : IVolumeCommand<SetVolumeCommand.Params> {
    @Serializable
    data class Params(val volumeLevel: Int)
}
