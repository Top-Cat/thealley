package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.OnkyoDevice

@Serializable
@SerialName("Onkyo")
data class OnkyoConfig(
    override val name: String,
    val host: String
) : IAlleyConfig {
    override fun deviceConfig() = OnkyoDeviceConfig(this)

    class OnkyoDeviceConfig(val config: OnkyoConfig) : IAlleyDeviceConfig<OnkyoDevice, OnkyoConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = OnkyoDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
