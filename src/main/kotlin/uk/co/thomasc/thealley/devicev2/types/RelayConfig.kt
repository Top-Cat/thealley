package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.relay.RelayDevice
import uk.co.thomasc.thealley.devicev2.relay.RelayState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Relay")
data class RelayConfig(
    override val name: String,
    val host: String,
    val apiKey: String,
    val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    val sensors: List<Int> = listOf()
) : IAlleyConfig {
    override fun deviceConfig() = RelayDeviceConfig(this)

    class RelayDeviceConfig(val config: RelayConfig) : IAlleyDeviceConfig<RelayDevice, RelayConfig, RelayState>() {
        override fun create(id: Int, state: RelayState, stateStore: IStateUpdater<RelayState>, dev: AlleyDeviceMapper) = RelayDevice(id, config, state, stateStore)
        override fun stateSerializer() = RelayState.serializer()
    }
}
