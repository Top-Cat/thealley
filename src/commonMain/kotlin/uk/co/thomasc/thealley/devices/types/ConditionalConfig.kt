package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.system.conditional.actions.IConditionAction
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition

@Serializable
@SerialName("Conditional")
data class ConditionalConfig(
    override val name: String,
    val conditions: List<ICondition>,
    val trigger: ICondition,
    val action: IConditionAction
) : IAlleyConfig
