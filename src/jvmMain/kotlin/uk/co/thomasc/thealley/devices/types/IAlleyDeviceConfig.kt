package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.json.Json
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.TexecomDevice
import uk.co.thomasc.thealley.devices.energy.bright.BrightDevice
import uk.co.thomasc.thealley.devices.energy.tado.TadoDevice
import uk.co.thomasc.thealley.devices.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devices.kasa.plug.PlugDevice
import uk.co.thomasc.thealley.devices.onkyo.OnkyoDevice
import uk.co.thomasc.thealley.devices.ps2.PS2Device
import uk.co.thomasc.thealley.devices.relay.RelayDevice
import uk.co.thomasc.thealley.devices.somfy.SomfyBlindDevice
import uk.co.thomasc.thealley.devices.somfy.SomfyGroupDevice
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.devices.switch.SwitchDevice
import uk.co.thomasc.thealley.devices.switch.SwitchServerDevice
import uk.co.thomasc.thealley.devices.system.conditional.ConditionalDevice
import uk.co.thomasc.thealley.devices.system.mqtt.MqttDevice
import uk.co.thomasc.thealley.devices.system.scene.SceneDevice
import uk.co.thomasc.thealley.devices.system.schedule.ScheduleDevice
import uk.co.thomasc.thealley.devices.system.sun.SunDevice
import uk.co.thomasc.thealley.devices.unifi.UnifiDevice
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionDevice
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindDevice
import uk.co.thomasc.thealley.devices.zigbee.candeo.CDimmerDevice
import uk.co.thomasc.thealley.devices.zigbee.moes.DualDimmerDevice
import uk.co.thomasc.thealley.devices.zigbee.moes.DualSwitchDevice
import uk.co.thomasc.thealley.devices.zigbee.moes.MDimmerDevice
import uk.co.thomasc.thealley.devices.zigbee.plug.ZPlugDevice
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialLightDevice
import uk.co.thomasc.thealley.devices.zigbee.relay.PartialRelayDevice
import uk.co.thomasc.thealley.devices.zigbee.samotech.SDimmerDevice
import uk.co.thomasc.thealley.devices.zigbee.zbmini.ZBMiniDevice
import uk.co.thomasc.thealley.system.StateUpdaterFactory

class GenericAlleyDeviceConfig<T : AlleyDevice<T, U, V>, U : IAlleyConfig<V>, V : IAlleyState> private constructor(config: U, val block: (Int, V, IStateUpdater<V>, AlleyDeviceMapper) -> T) : IAlleyDeviceConfig<T, U, V>(config) {
    override fun create(id: Int, state: V, stateStore: IStateUpdater<V>, dev: AlleyDeviceMapper) =
        block(id, state, stateStore, dev)

    companion object {
        fun <T : AlleyDevice<T, U, V>, U : IAlleyConfig<V>, V : IAlleyState> fromConfig(config: U, block: (Int, U, V, IStateUpdater<V>, AlleyDeviceMapper) -> T) =
            GenericAlleyDeviceConfig(config) { a: Int, b: V, c: IStateUpdater<V>, d: AlleyDeviceMapper -> block(a, config, b, c, d) }
        fun <T : AlleyDevice<T, U, V>, U : IAlleyConfig<V>, V : IAlleyState> fromConfig(config: U, block: (Int, U, V, IStateUpdater<V>) -> T) =
            GenericAlleyDeviceConfig(config) { a: Int, b: V, c: IStateUpdater<V>, _ -> block(a, config, b, c) }
    }
}

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
            is BlindConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::BlindDevice)
            is BrightConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::BrightDevice)
            is BulbConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::BulbDevice)
            is CDimmerConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::CDimmerDevice)
            is ConditionalConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::ConditionalDevice)
            is DualDimmerConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::DualDimmerDevice)
            is DualSwitchConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::DualSwitchDevice)
            is MDimmerConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::MDimmerDevice)
            is MotionConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::MotionDevice)
            is MqttConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::MqttDevice)
            is OnkyoConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::OnkyoDevice)
            is PartialLightConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::PartialLightDevice)
            is PartialRelayConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::PartialRelayDevice)
            is PlugConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::PlugDevice)
            is PS2Config -> GenericAlleyDeviceConfig.fromConfig(config, ::PS2Device)
            is RelayConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::RelayDevice)
            is SDimmerConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SDimmerDevice)
            is SceneConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SceneDevice)
            is ScheduleConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::ScheduleDevice)
            is SomfyBlindConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SomfyBlindDevice)
            is SomfyGroupConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SomfyGroupDevice)
            is SunConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SunDevice)
            is SwitchConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SwitchDevice)
            is SwitchServerConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::SwitchServerDevice)
            is TadoConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::TadoDevice)
            is TexecomConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::TexecomDevice)
            is UnifiConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::UnifiDevice)
            is ZBMiniConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::ZBMiniDevice)
            is ZBMini2Config -> GenericAlleyDeviceConfig.fromConfig(config, ::ZBMini2Device)
            is ZPlugConfig -> GenericAlleyDeviceConfig.fromConfig(config, ::ZPlugDevice)
            else -> throw IllegalArgumentException("Invalid config type ${config.javaClass.simpleName}")
        }
    }
}

fun IAlleyConfigBase.deviceConfig() = IAlleyDeviceConfig.fromConfig(this)
