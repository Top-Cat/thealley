package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.zbmini.ZBMiniDevice

@Serializable
@SerialName("ZBMini")
data class ZBMiniConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig, IZigbeeConfig {
    override fun deviceConfig() = ZBMiniDeviceConfig(this)

    class ZBMiniDeviceConfig(val config: ZBMiniConfig) : IAlleyDeviceConfig<ZBMiniDevice, ZBMiniConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = ZBMiniDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
