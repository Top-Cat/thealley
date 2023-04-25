package uk.co.thomasc.thealley.devices

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.client.ZigbeeUpdate
import uk.co.thomasc.thealley.client.jackson
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class BlindMotorUpdate(
    // Generic
    override val linkquality: Int,
    override val battery: Int?,

    // Blind motor
    val device_temperature: Int?,
    val motor_state: BlindMotorState?,
    val position: Int?,
    val power_outage_count: Int?,
    val running: Boolean?,
    val state: BlindState?
) : ZigbeeUpdate

enum class BlindMotorState { DECLINING, RISING, PAUSE, BLOCKED }
enum class BlindState { ON, OFF }

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

    fun handleMessage(node: JsonNode) {
        val message = jackson.treeToValue<BlindMotorUpdate>(node)!!

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
