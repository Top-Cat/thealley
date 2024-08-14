package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.KSerializer
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventEmitter
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyRelay
import uk.co.thomasc.thealley.devices.state.zigbee.IZigbeeState
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.IZigbeeConfig
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeDevice

abstract class ZigbeeRelayDevice<X : ZigbeeUpdateRelay, T : AlleyDevice<T, U, V>, U : IZigbeeConfig<V>, V : IZigbeeState>(
    id: Int,
    config: U,
    state: V,
    stateStore: IStateUpdater<V>,
    serializer: KSerializer<X>
) : ZigbeeDevice<X, T, U, V>(id, config, state, stateStore, serializer), IAlleyRelay {

    private suspend fun setLightState(bus: AlleyEventEmitter, state: ZRelayAction) {
        bus.emit(MqttSendEvent.from("${config.prefix}/${config.deviceId}/set", ZRelaySet(state)))
    }

    override suspend fun setPowerState(bus: AlleyEventEmitter, value: Boolean) = setLightState(bus, if (value) ZRelayAction.ON else ZRelayAction.OFF)

    override suspend fun getPowerState() = getState().state == ZRelayAction.ON

    override suspend fun togglePowerState(bus: AlleyEventEmitter) = setLightState(bus, ZRelayAction.TOGGLE)
}
