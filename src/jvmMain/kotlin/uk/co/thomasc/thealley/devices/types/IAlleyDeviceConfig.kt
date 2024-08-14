package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.system.StateUpdaterFactory

@Serializable
sealed class IAlleyDeviceConfig<T : AlleyDevice<T, U, V>, U : IAlleyConfig<V>, V : IAlleyState>(val config: U) {
    fun generate(id: Int, json: Json, stateUpdateFactory: StateUpdaterFactory, state: String, dev: AlleyDeviceMapper): T {
        val serializer = config.stateSerializer

        val stateObj = json.decodeFromString(serializer, state)
        val stateUpdater = stateUpdateFactory.getUpdater(serializer)

        return create(id, stateObj, stateUpdater, dev)
    }
    abstract fun create(id: Int, state: V, stateStore: IStateUpdater<V>, dev: AlleyDeviceMapper): T

    companion object {
        fun fromConfig(config: IAlleyConfigBase) = when (config) {
            is BlindConfig -> BlindDeviceConfig(config)
            is BrightConfig -> BrightDeviceConfig(config)
            is BulbConfig -> BulbDeviceConfig(config)
            is CDimmerConfig -> CDimmerDeviceConfig(config)
            is ConditionalConfig -> ConditionalDeviceConfig(config)
            is DualDimmerConfig -> DualDimmerDeviceConfig(config)
            is DualSwitchConfig -> DualSwitchDeviceConfig(config)
            is MDimmerConfig -> MDimmerDeviceConfig(config)
            is MotionConfig -> MotionDeviceConfig(config)
            is MqttConfig -> MqttDeviceConfig(config)
            is OnkyoConfig -> OnkyoDeviceConfig(config)
            is PartialLightConfig -> PartialLightDeviceConfig(config)
            is PartialRelayConfig -> PartialRelayDeviceConfig(config)
            is PlugConfig -> PlugDeviceConfig(config)
            is PS2Config -> PS2DeviceConfig(config)
            is RelayConfig -> RelayDeviceConfig(config)
            is SDimmerConfig -> SDimmerDeviceConfig(config)
            is SceneConfig -> SceneDeviceConfig(config)
            is ScheduleConfig -> ScheduleDeviceConfig(config)
            is SomfyBlindConfig -> SomfyBlindDeviceConfig(config)
            is SomfyGroupConfig -> SomfyGroupDeviceConfig(config)
            is SunConfig -> SunDeviceConfig(config)
            is SwitchConfig -> SwitchDeviceConfig(config)
            is SwitchServerConfig -> SwitchServerDeviceConfig(config)
            is TadoConfig -> TadoDeviceConfig(config)
            is TexecomConfig -> TexecomDeviceConfig(config)
            is UnifiConfig -> UnifiDeviceConfig(config)
            is ZBMiniConfig -> ZBMiniDeviceConfig(config)
            is ZPlugConfig -> ZPlugDeviceConfig(config)
            else -> throw IllegalArgumentException("Invalid config type ${config.javaClass.simpleName}")
        }
    }
}

fun IAlleyConfigBase.deviceConfig() = IAlleyDeviceConfig.fromConfig(this)
