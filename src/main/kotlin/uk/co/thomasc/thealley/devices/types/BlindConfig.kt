package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindDevice
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindState

@Serializable
@SerialName("Blind")
data class BlindConfig(
    override val name: String,
    override val deviceId: String,
    override val prefix: String = "zigbee"
) : IAlleyConfig, IZigbeeConfig {
    override fun deviceConfig() = BlindDeviceConfig(this)

    class BlindDeviceConfig(val config: BlindConfig) : IAlleyDeviceConfig<BlindDevice, BlindConfig, BlindState>() {
        override fun create(id: Int, state: BlindState, stateStore: IStateUpdater<BlindState>, dev: AlleyDeviceMapper) = BlindDevice(id, config, state, stateStore)
        override fun stateSerializer() = BlindState.serializer()
    }
}
