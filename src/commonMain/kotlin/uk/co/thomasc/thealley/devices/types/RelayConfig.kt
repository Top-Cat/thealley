package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.IConfigEditable
import uk.co.thomasc.thealley.devices.SimpleConfigEditable
import uk.co.thomasc.thealley.devices.fieldEditor
import uk.co.thomasc.thealley.devices.state.relay.RelayState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Relay")
data class RelayConfig(
    override val name: String,
    val host: String,
    val apiKey: String,
    override val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    override val sensors: List<Int> = listOf()
) : IAlleyConfig<RelayState>,
    IAlleyRelayConfig,
    ITriggerableConfig<RelayState>,
    IConfigEditable<RelayConfig> by SimpleConfigEditable(
        listOf(
            RelayConfig::name.fieldEditor("Name") { c, n -> c.copy(name = n) },
            RelayConfig::host.fieldEditor("Host") { c, n -> c.copy(host = n) },
            RelayConfig::apiKey.fieldEditor("API Key") { c, n -> c.copy(apiKey = n) },
            RelayConfig::timeout.fieldEditor("Timeout") { c, n -> c.copy(timeout = n) },
            RelayConfig::switchTimeout.fieldEditor("Switch Timeout") { c, n -> c.copy(switchTimeout = n) },
            RelayConfig::sensors.fieldEditor("Sensors", { it is MotionConfig }) { c, n -> c.copy(sensors = n) }
        )
    ) {
    override val defaultState = RelayState(false)
    override val stateSerializer = RelayState.serializer()
}
