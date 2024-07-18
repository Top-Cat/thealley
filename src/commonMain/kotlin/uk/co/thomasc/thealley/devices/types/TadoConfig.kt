package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor

@Serializable
@SerialName("Tado")
data class TadoConfig(
    override val name: String,
    val email: String,
    val pass: String,
    val updateReadings: Boolean = false
) : IAlleyConfig,
    IConfigEditable<TadoConfig> by SimpleConfigEditable(
        listOf(
            TadoConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            TadoConfig::email.fieldEditor("Email") { c, n -> c.copy(email = n) },
            TadoConfig::pass.fieldEditor("Password", password = true) { c, n -> c.copy(pass = n) },
            TadoConfig::updateReadings.fieldEditor("Update readings") { c, n -> c.copy(updateReadings = n) }
        )
    )
