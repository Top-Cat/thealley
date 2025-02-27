package uk.co.thomasc.thealley.devices.zigbee.samotech

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.kasa.bulb.TriggerHelper
import uk.co.thomasc.thealley.devices.state.zigbee.SamotechState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.sun.NightBrightnessCalc
import uk.co.thomasc.thealley.devices.types.SDimmerConfig
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeDimmerDevice
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import kotlin.math.abs

class SDimmerDevice(id: Int, config: SDimmerConfig, state: SamotechState, stateStore: IStateUpdater<SamotechState>) :
    ZigbeeDimmerDevice<SDimmerUpdate, SDimmerDevice, SDimmerConfig, SamotechState>(id, config, state, stateStore, SDimmerUpdate.serializer()), IAlleyLight {

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

    override suspend fun setComplexState(bus: AlleyEventEmitter, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        val newBrightness = if (lightState.brightness == null) null else lightState.brightness255()
        updateState(state.copy(lastBrightness = newBrightness))

        super.setComplexState(bus, lightState, transitionTime)
    }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) {
        val newBrightness = if (value) getState().brightness else null
        updateState(state.copy(lastBrightness = newBrightness))

        super.setPowerState(bus, value)
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: SDimmerUpdate) {
        val newBrightness = if (update.state == ZRelayAction.OFF) null else update.brightness

        if (abs((newBrightness ?: 1024) - (state.lastBrightness ?: 1024)) > 10) {
            logger.info { "SDimmer ignoring motion $newBrightness ${state.lastBrightness}" }
            updateState(state.copy(ignoreMotionUntil = Clock.System.now().plus(config.switchTimeout), lastBrightness = newBrightness))
        }

        bus.emit(ReportStateEvent(this))
    }

    companion object : KLogging()
}
