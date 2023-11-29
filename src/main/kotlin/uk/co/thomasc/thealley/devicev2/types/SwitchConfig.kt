package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.switch.SwitchDevice
import uk.co.thomasc.thealley.devicev2.switch.SwitchState

@Serializable
@SerialName("Switch")
data class SwitchConfig(override val name: String, val id: Int, val scenes: List<Int>) : IAlleyConfig {
    override fun deviceConfig() = SwitchDeviceConfig(this)

    class SwitchDeviceConfig(val config: SwitchConfig) : IAlleyDeviceConfig<SwitchDevice, SwitchConfig, SwitchState>() {
        override fun create(id: Int, state: SwitchState, stateStore: IStateUpdater<SwitchState>, dev: AlleyDeviceMapper) = SwitchDevice(id, config, state, stateStore)
        override fun stateSerializer() = SwitchState.serializer()
    }
}
