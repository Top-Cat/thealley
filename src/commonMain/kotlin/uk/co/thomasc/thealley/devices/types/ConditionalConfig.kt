package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.IConfigField
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.system.conditional.actions.IConditionAction
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition
import kotlin.reflect.KProperty1

abstract class ConditionalConfigConfigField<T : Any>(
    override val name: String,
    private val field: KProperty1<ConditionalConfig, T>,
    override val setter: (ConditionalConfig, T) -> ConditionalConfig
) : IConfigField<ConditionalConfig, T>() {
    override val clazz = ConditionalConfig::class
    override val getter = { c: ConditionalConfig -> field.get(c) }
}

class ConditionActionConfigField(name: String, field: KProperty1<ConditionalConfig, IConditionAction>, setter: (ConditionalConfig, IConditionAction) -> ConditionalConfig) :
    ConditionalConfigConfigField<IConditionAction>(name, field, setter)

class ConditionConfigField(name: String, field: KProperty1<ConditionalConfig, ICondition>, setter: (ConditionalConfig, ICondition) -> ConditionalConfig) :
    ConditionalConfigConfigField<ICondition>(name, field, setter)

class ConditionListConfigField(name: String, field: KProperty1<ConditionalConfig, List<ICondition>>, setter: (ConditionalConfig, List<ICondition>) -> ConditionalConfig) :
    ConditionalConfigConfigField<List<ICondition>>(name, field, setter)

@Serializable
@SerialName("Conditional")
data class ConditionalConfig(
    override val name: String,
    val conditions: List<ICondition>,
    val trigger: ICondition,
    val action: IConditionAction
) : IAlleyConfig,
    IConfigEditable<ConditionalConfig> by SimpleConfigEditable(
        listOf(
            ConditionalConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            ConditionListConfigField("Conditions", ConditionalConfig::conditions) { c, n -> c.copy(conditions = n) },
            ConditionConfigField("Trigger", ConditionalConfig::trigger) { c, n -> c.copy(trigger = n) },
            ConditionActionConfigField("Action", ConditionalConfig::action) { c, n -> c.copy(action = n) }
        )
    )
