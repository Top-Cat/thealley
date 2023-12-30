package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.ActivateScene")
data class ActivateSceneCommand(override val params: Params) : IGoogleHomeCommand<ActivateSceneCommand.Params> {
    @Serializable
    data class Params(
        val deactivate: Boolean
    )
}
