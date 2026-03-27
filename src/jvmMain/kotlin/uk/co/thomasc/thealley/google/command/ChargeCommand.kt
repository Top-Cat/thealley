package uk.co.thomasc.thealley.google.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action.devices.commands.Charge")
data class ChargeCommand(override val params: Params) : IEnergyStorageCommand<ChargeCommand.Params> {
    @Serializable
    data class Params(val charge: Boolean)
}
