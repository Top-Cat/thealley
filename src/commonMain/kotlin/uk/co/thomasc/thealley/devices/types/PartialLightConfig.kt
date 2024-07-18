package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("PartialLight")
data class PartialLightConfig(
    override val name: String,
    val device: Int,
    val index: Int
) : IAlleyConfig,
    IConfigEditable<PartialLightConfig> by SimpleConfigEditable(
        listOf(
            PartialLightConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            PartialLightConfig::device.fieldEditor("Device", { it is IAlleyDualLightConfig }) { c, n -> c.copy(device = n) },
            PartialLightConfig::index.fieldEditor("Index") { c, n -> c.copy(device = n) }
        )
    )
