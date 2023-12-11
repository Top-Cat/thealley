package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.unifi.UnifiDevice

@Serializable
@SerialName("Unifi")
data class UnifiConfig(
    override val name: String,
    val mainNetwork: String,
    val guestNetwork: String,
    val guestPassword: String
) : IAlleyConfig {
    override fun deviceConfig() = UnifiDeviceConfig(this)

    class UnifiDeviceConfig(val config: UnifiConfig) : IAlleyDeviceConfig<UnifiDevice, UnifiConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = UnifiDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
