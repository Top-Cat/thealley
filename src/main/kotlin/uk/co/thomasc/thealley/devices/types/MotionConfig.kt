package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.xiaomi.aq2.MotionDevice

@Serializable
@SerialName("Motion")
data class MotionConfig(override val name: String, val deviceId: String) : IAlleyConfig {
    override fun deviceConfig() = MotionDeviceConfig(this)

    class MotionDeviceConfig(val config: MotionConfig) : IAlleyDeviceConfig<MotionDevice, MotionConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MotionDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
