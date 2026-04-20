package uk.co.thomasc.thealley.devices.zigbee.custom

import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IAlleyStats
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.state.zigbee.blind.BlindState
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.types.ZBlindConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice
import uk.co.thomasc.thealley.devices.zigbee.blind.BlindCommand
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.EnergyStorageTrait
import uk.co.thomasc.thealley.google.trait.IBlindState
import uk.co.thomasc.thealley.google.trait.OpenCloseTrait
import kotlin.math.roundToInt

class ZBlindDevice(id: Int, config: ZBlindConfig, state: BlindState, stateStore: IStateUpdater<BlindState>) :
    ZigbeeDevice<BlindUpdate, ZBlindDevice, ZBlindConfig, BlindState>(id, config, state, stateStore, BlindUpdate.serializer()), IAlleyLight, IAlleyStats {

    override suspend fun onInit(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.BLINDS,
            true,
            OpenCloseTrait(
                getPosition = {
                    IBlindState.SingleDirection(state.position ?: 0)
                },
                setPosition = {
                    setPosition(bus, it)
                }
            ),
            EnergyStorageTrait(
                queryOnlyEnergyStorage = true,
                getChargeState = {
                    EnergyStorageTrait.State(
                        EnergyStorageTrait.GoogleDescriptiveCapacity.MEDIUM,
                        listOfNotNull(
                            state.battery?.let { EnergyStorageTrait.GoogleCapacity(it, EnergyStorageTrait.CapacityUnit.PERCENTAGE) }
                        )
                    )
                }
            )
        )
    }

    override suspend fun onUpdate(bus: AlleyEventEmitter, update: BlindUpdate) {
        if (update.battery != null && update.battery < 20 && updateState(state.copy(lowBatNotificationState = true))) {
            bus.emit(LowBatteryEvent(id, update.battery))
        } else if (update.battery != null && update.battery > 50) {
            updateState(state.copy(lowBatNotificationState = false))
        }

        if (updateState(state.copy(position = update.position, battery = update.battery?.roundToInt()))) {
            bus.emit(ReportStateEvent(this))
        }

        props["temperature"] = JsonPrimitive(update.temperature.toDouble())
        update.humidity?.let {
            props["humidity"] = JsonPrimitive(update.humidity.toDouble())
        }
        update.battery?.let {
            props["battery"] = JsonPrimitive(update.battery.toDouble())
        }
    }

    suspend fun sendCommand(bus: AlleyEventEmitter, cmd: BlindCommand) =
        sendUpdate(bus, "{\"state\": \"$cmd\"}")

    private suspend fun setPosition(bus: AlleyEventEmitter, pos: Int) =
        sendUpdate(bus, "{\"position\": $pos}")

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) =
        sendCommand(bus, if (value) BlindCommand.OPEN else BlindCommand.CLOSE)

    override suspend fun getLightState() = IAlleyLight.LightState(state.position)

    override suspend fun setComplexState(bus: AlleyEventEmitter, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        lightState.brightness?.let {
            setPosition(bus, it)
        }
    }

    override suspend fun getPowerState() = (state.position ?: 0) > 0

    override suspend fun togglePowerState(bus: AlleyEventEmitter) = setPowerState(bus, !getPowerState())

    override val props: MutableMap<String, JsonPrimitive> = mutableMapOf()

    companion object : KLogging()
}
