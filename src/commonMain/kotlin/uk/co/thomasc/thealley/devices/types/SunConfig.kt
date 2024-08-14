package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.system.sun.SunState

@Serializable
@SerialName("Sun")
data class SunConfig(
    override val name: String,
    val lat: Double,
    val lon: Double,
    val tz: String
) : IAlleyConfig<SunState>,
    IConfigEditable<SunConfig> by SimpleConfigEditable(
        listOf(
            SunConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            SunConfig::lat.fieldEditor("Latitude") { c, n -> c.copy(lat = n) },
            SunConfig::lon.fieldEditor("Longitude") { c, n -> c.copy(lon = n) },
            SunConfig::tz.fieldEditor("Timezone") { c, n -> c.copy(tz = n) }
        )
    ) {
    override val defaultState = SunState(true)
    override val stateSerializer = SunState.serializer()
}
