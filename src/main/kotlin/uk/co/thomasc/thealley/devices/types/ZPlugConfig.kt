package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.plug.ZPlugDevice

@Serializable
@SerialName("ZPlug")
data class ZPlugConfig(
    override val name: String,
    val deviceId: String,
    val prefix: String = "zigbee"
) : IAlleyConfig {
    override fun deviceConfig() = ZPlugDeviceConfig(this)

    class ZPlugDeviceConfig(val config: ZPlugConfig) : IAlleyDeviceConfig<ZPlugDevice, ZPlugConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = ZPlugDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
