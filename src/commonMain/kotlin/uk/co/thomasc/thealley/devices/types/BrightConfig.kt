package uk.co.thomasc.thealley.devices.types

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.energy.bright.BrightState

@Serializable
@SerialName("Bright")
data class BrightConfig(
    override val name: String,
    val email: String,
    val pass: String
) : IAlleyConfig<BrightState>,
    IConfigEditable<BrightConfig> by SimpleConfigEditable(
        listOf(
            BrightConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            BrightConfig::email.fieldEditor("Email") { c, n -> c.copy(email = n) },
            BrightConfig::pass.fieldEditor("Password", password = true) { c, n -> c.copy(pass = n) }
        )
    ) {
    override val defaultState = BrightState(0.0, Clock.System.now(), Clock.System.now())
    override val stateSerializer = BrightState.serializer()
}
