package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.EmptyState
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.system.mqtt.MqttDevice

@Serializable
@SerialName("Mqtt")
data class MqttConfig(override val name: String, val clientId: String, val host: String, val user: String, val pass: String) : IAlleyConfig {
    override fun deviceConfig() = MqttDeviceConfig(this)

    class MqttDeviceConfig(val config: MqttConfig) : IAlleyDeviceConfig<MqttDevice, MqttConfig, EmptyState>() {
        override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MqttDevice(id, config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}
