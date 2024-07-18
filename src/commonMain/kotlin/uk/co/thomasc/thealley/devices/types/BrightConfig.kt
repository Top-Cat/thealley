package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("Bright")
data class BrightConfig(
    override val name: String,
    val email: String,
    val pass: String
) : IAlleyConfig,
    IConfigEditable<BrightConfig> by SimpleConfigEditable(
        listOf(
            BrightConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            BrightConfig::email.fieldEditor("Email") { c, n -> c.copy(email = n) },
            BrightConfig::pass.fieldEditor("Password", password = true) { c, n -> c.copy(pass = n) }
        )
    )
