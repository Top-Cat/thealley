package uk.co.thomasc.thealley.devices.system.conditional.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Relay")
data class RelayConditionAction(val deviceId: Int, val state: Boolean) : IConditionAction
