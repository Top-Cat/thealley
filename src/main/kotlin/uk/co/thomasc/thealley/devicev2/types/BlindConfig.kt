package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.xiaomi.blind.BlindDevice
import uk.co.thomasc.thealley.devicev2.xiaomi.blind.BlindState

@Serializable
@SerialName("Blind")
data class BlindConfig(override val name: String, val deviceId: String) : IAlleyConfig {
    override fun deviceConfig() = BlindDeviceConfig(this)

    class BlindDeviceConfig(val config: BlindConfig) : IAlleyDeviceConfig<BlindDevice, BlindConfig, BlindState>() {
        override fun create(id: Int, state: BlindState, stateStore: IStateUpdater<BlindState>, dev: AlleyDeviceMapper) = BlindDevice(id, config, state, stateStore)
        override fun stateSerializer() = BlindState.serializer()
    }
}
