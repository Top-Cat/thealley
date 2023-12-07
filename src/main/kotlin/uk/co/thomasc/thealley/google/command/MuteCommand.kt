package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.mute")
data class MuteCommand(override val params: Params) : IVolumeCommand<MuteCommand.Params> {
    @Serializable
    data class Params(val mute: Boolean)
}
