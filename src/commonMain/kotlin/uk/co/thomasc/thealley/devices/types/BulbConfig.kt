package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Bulb")
data class BulbConfig(
    override val name: String,
    override val host: String,
    val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    val sensors: List<Int> = listOf()
) : IAlleyConfig,
    IKasaConfig,
    IAlleyLightConfig,
    IConfigEditable<BulbConfig> by SimpleConfigEditable(
        listOf(
            BulbConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            BulbConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) },
            BulbConfig::timeout.fieldEditor("Timeout") { c, n -> c.copy(timeout = n) },
            BulbConfig::switchTimeout.fieldEditor("Switch Timeout") { c, n -> c.copy(switchTimeout = n) },
            BulbConfig::sensors.fieldEditor("Sensors", { it is MotionConfig }) { c, n -> c.copy(sensors = n) }
        )
    )
