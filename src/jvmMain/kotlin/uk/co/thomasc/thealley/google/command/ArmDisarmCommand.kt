package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.ArmDisarm")
data class ArmDisarmCommand(override val params: Params) : IGoogleHomeCommand<ArmDisarmCommand.Params> {
    @Serializable
    data class Params(
        val followUpToken: String? = null,
        val arm: Boolean,
        val cancel: Boolean? = null,
        val armLevel: String? = null
    )
}
