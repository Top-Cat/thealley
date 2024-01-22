package uk.co.thomasc.thealley.devices.zigbee.relay

import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyMultiGangRelay
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.PartialRelayConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.OnOffTrait

class PartialRelayDevice(id: Int, config: PartialRelayConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>, val dev: AlleyDeviceMapper) :
    AlleyDevice<PartialRelayDevice, PartialRelayConfig, EmptyState>(id, config, state, stateStore), IAlleyRelay {

    private suspend fun getDevice() = dev.getDevice(config.device)

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
