package uk.co.thomasc.thealley.devices.zigbee.relay

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IAlleyLight
import uk.co.thomasc.thealley.devices.IAlleyMultiGangLight
import uk.co.thomasc.thealley.devices.IAlleyMultiGangRelay
import uk.co.thomasc.thealley.devices.IAlleyRelay
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.PartialLightConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.BrightnessTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class PartialLightDevice(id: Int, config: PartialLightConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<PartialLightDevice, PartialLightConfig, EmptyState>(id, config, state, stateStore), IAlleyLight {

    private suspend fun getDevice() = dev.getDevice(config.device)
    override suspend fun getLightState() =
        when (val device = getDevice()) {
            is IAlleyMultiGangLight -> device.getLightState(config.index)
            is IAlleyLight -> device.getLightState()
            else -> {
                logger.warn { "Target device ${config.device} is not a light" }
                IAlleyLight.LightState()
            }
        }

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        when (val device = getDevice()) {
            is IAlleyMultiGangLight -> device.setComplexState(bus, config.index, lightState, transitionTime)
            is IAlleyLight -> device.setComplexState(bus, lightState, transitionTime)
            else -> logger.warn { "Target device ${config.device} is not a light" }
        }
    }

    override suspend fun setPowerState(bus: AlleyEventBus, value: Boolean) {
        when (val device = getDevice()) {
            is IAlleyMultiGangRelay -> device.setPowerState(bus, config.index, value)
            is IAlleyRelay -> device.setPowerState(bus, value)
            else -> logger.warn { "Target device ${config.device} is not a relay" }
        }
    }

    override suspend fun getPowerState() =
        when (val device = getDevice()) {
            is IAlleyMultiGangRelay -> device.getPowerState(config.index)
            is IAlleyRelay -> device.getPowerState()
            else -> {
                logger.warn { "Target device ${config.device} is not a relay" }
                false
            }
        }

    override suspend fun togglePowerState(bus: AlleyEventBus) {
        when (val device = getDevice()) {
            is IAlleyMultiGangRelay -> device.togglePowerState(bus, config.index)
            is IAlleyRelay -> device.togglePowerState(bus)
            else -> logger.warn { "Target device ${config.device} is not a relay" }
        }
    }

    override suspend fun init(bus: AlleyEventBus) {
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
                    getLightState().brightness ?: 0
                },
                setBrightness = { b ->
                    setComplexState(bus, IAlleyLight.LightState(b))
                }
            )
        )

        bus.handle<MultiGangUpdate> { ev ->
            if (ev.device == config.device) {
                bus.emit(ReportStateEvent(this))
            }
        }
    }

    companion object : KLogging()
}
