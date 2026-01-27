package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.leeds.bin.LeedsBinState

@Serializable
@SerialName("LeedsBin")
data class LeedsBinConfig(
    override val name: String,
    val apiKey: String,
    val uprn: Int
) : IAlleyConfig<LeedsBinState>,
    IConfigEditable<LeedsBinConfig> by SimpleConfigEditable(
        listOf(
            LeedsBinConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            LeedsBinConfig::apiKey.fieldEditor("Api Key") { c, n -> c.copy(apiKey = n) },
            LeedsBinConfig::uprn.fieldEditor("Unique Property Reference Number") { c, n -> c.copy(uprn = n) }
        )
    ) {
    override val defaultState = LeedsBinState()
    override val stateSerializer = LeedsBinState.serializer()
}
