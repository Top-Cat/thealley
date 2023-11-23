package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.eclipse.paho.client.mqttv3.MqttMessage
import uk.co.thomasc.thealley.client.RelayMqtt
import uk.co.thomasc.thealley.client.ZigbeeUpdate
import uk.co.thomasc.thealley.client.alleyJson
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class ZPlugData(
    // Generic
    override val linkquality: Int,
    override val battery: Int?,

    // Specific
    val current: Float,
    val energy: Float,
    val power: Float,
    val power_outage_memory: String,
    val state: ZPlugState,
    val switch_type: String,
    val voltage: Float
) : ZigbeeUpdate

enum class ZPlugState { ON, OFF }

class ZPlug(
    private val deviceId: String,
    private val mqtt: RelayMqtt.DeviceGateway
) {
    private val latch = ReentrantLock()
    private val condition = latch.newCondition()

    private var plugData: ZPlugData? = null

    fun handleMessage(node: JsonElement) {
        val message = alleyJson.decodeFromJsonElement<ZPlugData>(node)

        plugData = message
        latch.withLock {
            condition.signalAll()
        }
    }

    suspend fun getState() = runBlocking {
        mqtt.sendToMqtt("zigbee/$deviceId/get", MqttMessage("{\"state\": \"\"}".toByteArray()))

        withContext(Dispatchers.IO) {
            latch.withLock {
                condition.await(5L, TimeUnit.SECONDS)
            }
        }

        plugData
    }
}
