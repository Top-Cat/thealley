package uk.co.thomasc.thealley.devices.zigbee.relay

import kotlinx.serialization.KSerializer
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.generic.IAlleyLight
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.IZigbeeConfig
import uk.co.thomasc.thealley.devices.zigbee.IZigbeeState
import uk.co.thomasc.thealley.devices.zigbee.samotech.ZDimmerSet

abstract class ZigbeeDimmerDevice<X : ZigbeeUpdateDimmer, T : AlleyDevice<T, U, V>, U : IZigbeeConfig, V : IZigbeeState>(
    id: Int,
    config: U,
    state: V,
    stateStore: IStateUpdater<V>,
    serializer: KSerializer<X>
) : ZigbeeRelayDevice<X, T, U, V>(id, config, state, stateStore, serializer), IAlleyLight {

    protected fun getBrightness() = (getState().brightness / 2.55f).toInt()

    override suspend fun getLightState() = IAlleyLight.LightState(getBrightness())

    override suspend fun setComplexState(bus: AlleyEventBus, lightState: IAlleyLight.LightState, transitionTime: Int?) {
        val json = ZDimmerSet(lightState.brightness255(), null, transitionTime?.let { it / 1000f })
        bus.emit(MqttSendEvent.from("${config.prefix}/${config.deviceId}/set", json))
    }
}
