package uk.co.thomasc.thealley.devices.zigbee

import kotlinx.serialization.KSerializer
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.IZigbeeConfig
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class ZigbeeDevice<X : ZigbeeUpdate, T : AlleyDevice<T, U, V>, U : IZigbeeConfig, V : IZigbeeState>(
    id: Int,
    config: U,
    state: V,
    stateStore: IStateUpdater<V>,
    private val serializer: KSerializer<X>
) : AlleyDevice<T, U, V>(id, config, state, stateStore) {
    private val latch = ReentrantLock()
    private val condition = latch.newCondition()

    private lateinit var helper: Zigbee2MqttHelper<X>
    protected fun getState() = helper.get()
    protected open fun getEvent() = MqttSendEvent("${config.prefix}/${config.deviceId}/get", "{\"state\": \"\"}")

    final override suspend fun init(bus: AlleyEventBus) {
        helper = Zigbee2MqttHelper(bus, config.prefix, config.deviceId, getEvent(), latch, condition, { json ->
            alleyJsonUgly.decodeFromString(serializer, json)
        }) {
            onUpdate(bus, getState())
        }

        onInit(bus)
    }

    open suspend fun onInit(bus: AlleyEventBus) {

    }

    protected fun waitForUpdate() {
        latch.withLock {
            condition.await(5L, TimeUnit.SECONDS)
        }
    }

    abstract suspend fun onUpdate(bus: AlleyEventBus, update: X)
}
