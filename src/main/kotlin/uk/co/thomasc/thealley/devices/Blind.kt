package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.client.ZigbeeUpdate
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class BlindCommand {
    OPEN, CLOSE, STOP
}

class Blind(
    private val deviceId: String,
    private val mqtt: RelayMqtt.DeviceGateway,
    private var state: Int? = null
) : Light<Unit> {
    private val latch = ReentrantLock()
    private val condition = latch.newCondition()

    override fun setPowerState(value: Boolean) =
        sendCommand(if (value) BlindCommand.OPEN else BlindCommand.CLOSE)

    override fun setComplexState(brightness: Int?, hue: Int?, saturation: Int?, temperature: Int?, transitionTime: Int?) {
        brightness?.let {
            setPosition(it)
        }
    }

    private fun sendCommand(cmd: BlindCommand) {
        mqtt.sendToMqtt("zigbee/$deviceId/set", MqttMessage("{\"state\": \"$cmd\"}".toByteArray()))
    }

    private fun setPosition(pos: Int) {
        mqtt.sendToMqtt("zigbee/$deviceId/set", MqttMessage("{\"position\": \"$pos\"}".toByteArray()))
    }

    override suspend fun getPowerState() = (getState() ?: 0) > 0

    override suspend fun togglePowerState() = setPowerState(!getPowerState())

    fun handleMessage(message: ZigbeeUpdate) {
        state = message.position
        latch.withLock {
            condition.signalAll()
        }
    }

    fun getState() = runBlocking {
        mqtt.sendToMqtt("zigbee/$deviceId/get", MqttMessage("{\"state\": \"\"}".toByteArray()))

        withContext(Dispatchers.IO) {
            latch.withLock {
                condition.await(5L, TimeUnit.SECONDS)
            }
        }

        state
    }
}
