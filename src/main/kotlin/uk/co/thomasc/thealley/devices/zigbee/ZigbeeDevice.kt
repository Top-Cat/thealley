package uk.co.thomasc.thealley.devices.zigbee

import kotlinx.serialization.KSerializer
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.types.IZigbeeConfig

abstract class ZigbeeDevice<X : ZigbeeUpdate, T : AlleyDevice<T, U, V>, U : IZigbeeConfig, V : IZigbeeState>(
    id: Int,
    config: U,
    state: V,
    stateStore: IStateUpdater<V>,
    private val serializer: KSerializer<X>
) : AlleyDevice<T, U, V>(id, config, state, stateStore) {

    private lateinit var helper: Zigbee2MqttHelper<X>
    protected fun getState() = helper.get()

    override suspend fun init(bus: AlleyEventBus) {
        helper = Zigbee2MqttHelper(bus, config.prefix, config.deviceId, { json ->
            alleyJsonUgly.decodeFromString(serializer, json)
        }) {
            onUpdate(bus, getState())
        }
    }

    abstract suspend fun onUpdate(bus: AlleyEventBus, state: X)
}
