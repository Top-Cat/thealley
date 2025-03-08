package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.esphome.MMWaveState

@Serializable
@SerialName("MMWave")
data class MMWaveConfig(
    override val name: String,
    val prefix: String
) : IAlleyConfig<MMWaveState>,
    IConfigEditable<MMWaveConfig> by SimpleConfigEditable(
        listOf(
            MMWaveConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            MMWaveConfig::prefix.fieldEditor("MQTT Prefix") { c, n -> c.copy(prefix = n) }
        )
    ) {
    override val defaultState = MMWaveState()
    override val stateSerializer = MMWaveState.serializer()
}
