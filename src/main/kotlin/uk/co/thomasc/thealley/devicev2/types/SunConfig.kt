package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.sun.SunDevice
import uk.co.thomasc.thealley.devicev2.sun.SunState

@Serializable
@SerialName("Sun")
data class SunConfig(override val name: String, val lat: Double, val lon: Double, val tz: String) : IAlleyConfig {
    override fun deviceConfig() = SunDeviceConfig(this)

    class SunDeviceConfig(val config: SunConfig) : IAlleyDeviceConfig<SunDevice, SunConfig, SunState>() {
        override fun create(id: Int, state: SunState, stateStore: IStateUpdater<SunState>, dev: AlleyDeviceMapper) = SunDevice(id, config, state, stateStore)
        override fun stateSerializer() = SunState.serializer()
    }
}
