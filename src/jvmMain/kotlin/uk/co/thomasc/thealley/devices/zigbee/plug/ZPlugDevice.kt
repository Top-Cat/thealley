package uk.co.thomasc.thealley.devices.zigbee.plug

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyRelay
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.ZPlugConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZPlugDevice(id: Int, config: ZPlugConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeRelayDevice<ZPlugUpdate, ZPlugDevice, ZPlugConfig, EmptyState>(id, config, state, stateStore, ZPlugUpdate.serializer()), IAlleyRelay {

    override suspend fun onInit(bus: AlleyEventBus) {
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

    override suspend fun onUpdate(bus: AlleyEventBus, update: ZPlugUpdate) {
        bus.emit(ReportStateEvent(this))
    }
}
