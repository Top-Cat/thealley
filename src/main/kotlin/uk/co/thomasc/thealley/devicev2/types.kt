package uk.co.thomasc.thealley.devicev2

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.mqtt.MqttDevice
import uk.co.thomasc.thealley.devicev2.relay.RelayDevice
import uk.co.thomasc.thealley.devicev2.relay.RelayState
import uk.co.thomasc.thealley.devicev2.sun.SunDevice
import uk.co.thomasc.thealley.devicev2.sun.SunState

@Serializable
sealed interface IAlleyConfig {
    fun deviceConfig(): IAlleyDeviceConfig<*, *, *>
}

@Serializable
data class SunConfig(val lat: Double, val lon: Double, val tz: String) : IAlleyConfig {
    override fun deviceConfig() = SunDeviceConfig(this)

    class SunDeviceConfig(val config: SunConfig) : IAlleyDeviceConfig<SunDevice, SunConfig, SunState>() {
        override fun create(state: SunState, stateStore: IStateUpdater<SunState>) = SunDevice(config, state, stateStore)
        override fun stateSerializer() = SunState.serializer()
    }
}

@Serializable
data class RelayConfig(val host: String, val apiKey: String) : IAlleyConfig {
    override fun deviceConfig() = RelayDeviceConfig(this)

    class RelayDeviceConfig(val config: RelayConfig) : IAlleyDeviceConfig<RelayDevice, RelayConfig, RelayState>() {
        override fun create(state: RelayState, stateStore: IStateUpdater<RelayState>) = RelayDevice(config, state, stateStore)
        override fun stateSerializer() = RelayState.serializer()
    }
}

@Serializable
data class MqttConfig(val clientId: String, val host: String, val user: String, val pass: String) : IAlleyConfig {
    override fun deviceConfig() = MqttDeviceConfig(this)

    class MqttDeviceConfig(val config: MqttConfig) : IAlleyDeviceConfig<MqttDevice, MqttConfig, EmptyState>() {
        override fun create(state: EmptyState, stateStore: IStateUpdater<EmptyState>) = MqttDevice(config, state, stateStore)
        override fun stateSerializer() = EmptyState.serializer()
    }
}

@Serializable
object EmptyState
