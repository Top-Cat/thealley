package uk.co.thomasc.thealley.devices.zigbee.moes

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.MDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeDimmerDevice
import uk.co.thomasc.thealley.devices.zigbee.samotech.ZDimmerSet
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class MDimmerDevice(id: Int, config: MDimmerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDimmerDevice<MDimmerUpdate, MDimmerDevice, MDimmerConfig, EmptyState>(id, config, state, stateStore, MDimmerUpdate.serializer()), IAlleyLight {

    override suspend fun onInit(bus: AlleyEventBus) {
        registerGoogleHomeDevice(
            DeviceType.LIGHT,
            true,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            ),
            BrightnessTrait(
                getBrightness = {
                    getBrightness()
                },
                setBrightness = { b ->
                    setComplexState(bus, IAlleyLight.LightState(b))
                }
            )
        )
    }

    override suspend fun onUpdate(bus: AlleyEventBus, update: MDimmerUpdate) {
        bus.emit(ReportStateEvent(this))
    }

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        val action = when {
            lightState.brightness == null || lightState.brightness == 0 -> ZRelayAction.OFF
            else -> ZRelayAction.ON
        }

        val json = ZDimmerSet(lightState.brightness255(), action, null)
        bus.emit(MqttSendEvent.from("${config.prefix}/${config.deviceId}/set", json))
    }
}
