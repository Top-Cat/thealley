package uk.co.thomasc.thealley.devices.zigbee.zbmini

import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyRelay
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.ZBMiniConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZBMiniDevice(id: Int, config: ZBMiniConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeRelayDevice<ZBMiniUpdate, ZBMiniDevice, ZBMiniConfig, EmptyState>(id, config, state, stateStore, ZBMiniUpdate.serializer()), IAlleyRelay {

    override suspend fun init(bus: AlleyEventBus) {
        super.init(bus)

        registerGoogleHomeDevice(
            DeviceType.LIGHT,
            true,
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = {
                    setPowerState(bus, it)
                }
            )
        )
    }

    override suspend fun onUpdate(bus: AlleyEventBus, state: ZBMiniUpdate) {
        bus.emit(ReportStateEvent(this))
    }
}
