package uk.co.thomasc.thealley.devices.zigbee.zbmini

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.ZBMini2Config
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZBMini2Device(id: Int, config: ZBMini2Config, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    ZigbeeRelayDevice<ZBMini2Update, ZBMini2Device, ZBMini2Config, EmptyState>(id, config, state, stateStore, ZBMini2Update.serializer()), IAlleyRelay {

    override suspend fun onInit(bus: AlleyEventBusShim) {
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

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: ZBMini2Update) {
        bus.emit(ReportStateEvent(this))
    }
}
