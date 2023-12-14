package uk.co.thomasc.thealley.devices.zigbee.plug

import mu.KLogging
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyRelay
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.ZPlugConfig
import uk.co.thomasc.thealley.devices.zigbee.Zigbee2MqttHelper
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZPlugDevice(id: Int, config: ZPlugConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<ZPlugDevice, ZPlugConfig, EmptyState>(id, config, state, stateStore), IAlleyRelay {

    private lateinit var zigbeeHelper: Zigbee2MqttHelper<ZPlugUpdate>

    private suspend fun setLightState(bus: AlleyEventBus, state: ZPlugAction) {
        bus.emit(MqttSendEvent("${config.prefix}/${config.deviceId}/set", ZPlugSet(state).toJson()))
    }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) = setLightState(bus, if (value) ZPlugAction.ON else ZPlugAction.OFF)

    override suspend fun getPowerState() = zigbeeHelper.get().state == ZPlugAction.ON

    override suspend fun togglePowerState(bus: AlleyEventBus) = setLightState(bus, ZPlugAction.TOGGLE)

    override suspend fun init(bus: AlleyEventBus) {
        zigbeeHelper = Zigbee2MqttHelper(bus, config.prefix, config.deviceId) {
            alleyJsonUgly.decodeFromString<ZPlugUpdate>(it)
        }

        registerGoogleHomeDevice(
            DeviceType.LIGHT,
            false,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            )
        )
    }

    companion object : KLogging()
}
