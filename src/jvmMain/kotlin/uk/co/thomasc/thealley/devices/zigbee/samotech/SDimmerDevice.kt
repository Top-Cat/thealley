package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.generic.IAlleyRevocable
import uk.co.thomasc.thealley.devices.kasa.bulb.TriggerHelper
import uk.co.thomasc.thealley.devices.state.kasa.bulb.BulbState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.types.SDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeDimmerDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class SDimmerDevice(id: Int, config: SDimmerConfig, state: BulbState, stateStore: IStateUpdater<BulbState>) :
    ZigbeeDimmerDevice<SDimmerUpdate, SDimmerDevice, SDimmerConfig, BulbState>(id, config, state, stateStore, SDimmerUpdate.serializer()), IAlleyLight, IAlleyRevocable {

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

        triggerHelper.init(bus, { now ->
            onWithNightScaling(bus, now)
        }) {
            setPowerState(bus, false)
        }
    }

    private suspend fun onWithNightScaling(bus: AlleyEventEmitter, now: Instant, transitionTime: Int = 500) =
        setComplexState(
            bus,
            IAlleyLight.LightState(
                NightBrightnessCalc.getBrightnessFor(now)
            ),
            transitionTime
        )

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: SDimmerUpdate) {
        bus.emit(ReportStateEvent(this))
    }

    override suspend fun hold() {
        updateState(state.copy(ignoreMotionUntil = Clock.System.now().plus(config.switchTimeout)))
    }

    override suspend fun revoke() {
        updateState(state.copy(ignoreMotionUntil = null))
    }
}
