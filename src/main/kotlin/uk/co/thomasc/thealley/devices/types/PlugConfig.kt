package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.kasa.plug.PlugDevice
import uk.co.thomasc.thealley.devices.kasa.plug.PlugState

@Serializable
@SerialName("Plug")
data class PlugConfig(override val name: String, override val host: String) : IAlleyConfig, IKasaConfig {
    override fun deviceConfig() = PlugDeviceConfig(this)

    class PlugDeviceConfig(val config: PlugConfig) : IAlleyDeviceConfig<PlugDevice, PlugConfig, PlugState>() {
        override fun create(id: Int, state: PlugState, stateStore: IStateUpdater<PlugState>, dev: AlleyDeviceMapper) = PlugDevice(id, config, state, stateStore)
        override fun stateSerializer() = PlugState.serializer()
    }
}
