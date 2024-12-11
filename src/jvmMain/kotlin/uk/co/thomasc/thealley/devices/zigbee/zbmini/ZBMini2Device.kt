package uk.co.thomasc.thealley.devices.zigbee.zbmini

import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.kasa.bulb.TriggerHelper
import uk.co.thomasc.thealley.devices.state.kasa.bulb.BulbState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.types.ZBMini2Config
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeRelayDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class ZBMini2Device(id: Int, config: ZBMini2Config, state: BulbState, stateStore: IStateUpdater<BulbState>) :
    ZigbeeRelayDevice<ZBMini2Update, ZBMini2Device, ZBMini2Config, BulbState>(id, config, state, stateStore, ZBMini2Update.serializer()), IAlleyRelay {

    private val triggerHelper = TriggerHelper(this) { this.state }

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

        triggerHelper.init(bus, { now ->
            setPowerState(bus, NightBrightnessCalc.getBrightnessFor(now) > 50)
        }) {
            setPowerState(bus, false)
        }
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: ZBMini2Update) {
        bus.emit(ReportStateEvent(this))
    }
}
