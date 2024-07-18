package uk.co.thomasc.thealley.devices.types

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.IConfigField
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import kotlin.reflect.KProperty1

data class ScheduleElementConfigField(
    override val name: String,
    val field: KProperty1<ScheduleConfig, List<ScheduleConfig.ScheduleElement>>,
    override val setter: (ScheduleConfig, List<ScheduleConfig.ScheduleElement>) -> ScheduleConfig
) : IConfigField<ScheduleConfig, List<ScheduleConfig.ScheduleElement>>() {
    override val clazz = ScheduleConfig::class
    override val getter = { c: ScheduleConfig -> field.get(c) }
}

@Serializable
@SerialName("Schedule")
data class ScheduleConfig(
    override val name: String,
    val device: Int,
    val elements: List<ScheduleElement> = listOf()
) : IAlleyConfig,
    IConfigEditable<ScheduleConfig> by SimpleConfigEditable(
        listOf(
            ScheduleConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            ScheduleConfig::device.fieldEditor("Device", { it is IAlleyRelayConfig }) { c, n -> c.copy(device = n) },
            ScheduleElementConfigField("Elements", ScheduleConfig::elements, { c, n -> c.copy(elements = n) })
        )
    ) {
    @Serializable
    data class ScheduleElement(
        val time: LocalTime,
        val state: Boolean
    )
}
