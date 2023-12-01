package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.xiaomi.aq2.MotionDevice

@Serializable
@SerialName("Motion")
data class MotionConfig(override val name: String, val deviceId: String) : IAlleyConfig {
    override fun deviceConfig() = MotionDeviceConfig(this)

    class MotionDeviceConfig(val config: MotionConfig) : IAlleyDeviceConfig<MotionDevice, MotionConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MotionDevice(id, config, state, stateStore, dev)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
