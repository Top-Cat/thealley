package uk.co.thomasc.thealley.devices.zigbee.candeo

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.CDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeDimmerDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class CDimmerDevice(id: Int, config: CDimmerConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeDimmerDevice<CDimmerUpdate, CDimmerDevice, CDimmerConfig, EmptyState>(id, config, state, stateStore, CDimmerUpdate.serializer()), IAlleyLight {

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

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: CDimmerUpdate) {
        bus.emit(ReportStateEvent(this))
    }
}
