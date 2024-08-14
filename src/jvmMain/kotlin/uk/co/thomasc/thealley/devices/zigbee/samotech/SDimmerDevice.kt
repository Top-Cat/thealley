package uk.co.thomasc.thealley.devices.zigbee.samotech

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.SDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeDimmerDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class SDimmerDevice(id: Int, config: SDimmerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDimmerDevice<SDimmerUpdate, SDimmerDevice, SDimmerConfig, EmptyState>(id, config, state, stateStore, SDimmerUpdate.serializer()), IAlleyLight {

    override suspend fun onInit(bus: AlleyEventBusShim) {
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

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: SDimmerUpdate) {
        bus.emit(ReportStateEvent(this))
    }
}
