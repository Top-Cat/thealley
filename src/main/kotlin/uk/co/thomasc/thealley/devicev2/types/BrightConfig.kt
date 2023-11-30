package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.energy.bright.BrightDevice
import uk.co.thomasc.thealley.devicev2.energy.bright.BrightState

@Serializable
@SerialName("Bright")
data class BrightConfig(override val name: String, val email: String, val pass: String) : IAlleyConfig {
    override fun deviceConfig() = BrightDeviceConfig(this)

    class BrightDeviceConfig(val config: BrightConfig) : IAlleyDeviceConfig<BrightDevice, BrightConfig, BrightState>() {
        override fun create(id: Int, state: BrightState, stateStore: IStateUpdater<BrightState>, dev: AlleyDeviceMapper) = BrightDevice(id, config, state, stateStore)
        override fun stateSerializer() = BrightState.serializer()
    }
}
