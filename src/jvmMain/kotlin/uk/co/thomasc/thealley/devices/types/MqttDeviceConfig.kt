package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttDevice

class MqttDeviceConfig(val config: MqttConfig) : IAlleyDeviceConfig<MqttDevice, MqttConfig, EmptyState>() {
    override fun create(id: Int, state: EmptyState, stateStore: IStateUpdater<EmptyState>, dev: AlleyDeviceMapper) = MqttDevice(id, config, state, stateStore)
    override fun stateSerializer() = EmptyState.serializer()
}
