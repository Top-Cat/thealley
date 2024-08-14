package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.coroutines.launch
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.generic.IAlleyMultiGangLight
import uk.co.thomasc.thealley.devices.generic.IAlleyMultiGangRelay
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.EmptyState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.PartialLightConfig
import uk.co.thomasc.thealley.devices.zigbee.Zigbee2MqttHelper
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

    override suspend fun setComplexState(bus: AlleyEventEmitter, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        when (val device = getDevice()) {
            is IAlleyMultiGangLight -> device.setComplexState(bus, config.index, lightState, transitionTime)
            is IAlleyLight -> device.setComplexState(bus, lightState, transitionTime)
            else -> logger.warn { "Target device ${config.device} is not a light" }
        }
    }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) {
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

    override suspend fun togglePowerState(bus: AlleyEventEmitter) {
        when (val device = getDevice()) {
            is IAlleyMultiGangRelay -> device.togglePowerState(bus, config.index)
            is IAlleyRelay -> device.togglePowerState(bus)
            else -> logger.warn { "Target device ${config.device} is not a relay" }
        }
    }

    override suspend fun init(bus: AlleyEventBusShim) {
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
                    Zigbee2MqttHelper.scope.launch {
                        setComplexState(bus, IAlleyLight.LightState(b))
                    }
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
