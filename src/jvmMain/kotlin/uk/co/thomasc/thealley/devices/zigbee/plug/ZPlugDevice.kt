package uk.co.thomasc.thealley.devices.zigbee.plug

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.ZPlugConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZPlugDevice(id: Int, config: ZPlugConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeRelayDevice<ZPlugUpdate, ZPlugDevice, ZPlugConfig, EmptyState>(id, config, state, stateStore, ZPlugUpdate.serializer()), IAlleyRelay {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.OUTLET,
            true,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            )
        )
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: ZPlugUpdate) {
        bus.emit(ReportStateEvent(this))
    }
}
