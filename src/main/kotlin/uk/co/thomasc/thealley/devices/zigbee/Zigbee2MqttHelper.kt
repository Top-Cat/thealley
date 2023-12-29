package uk.co.thomasc.thealley.devices.zigbee

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import uk.co.thomasc.thealley.cached
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.GetStateException
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.hours

class Zigbee2MqttHelper<T : ZigbeeUpdate>(
    private val bus: AlleyEventBus,
    private val prefix: String,
    private val deviceId: String,
    private val decode: suspend (String) -> T,
    private val callback: suspend () -> Unit = {}
) {
    private val latch = ReentrantLock()
    private val condition = latch.newCondition()

    private var latestUpdate: T by cached(1.hours) {
        bus.emit(MqttSendEvent("$prefix/$deviceId/get", "{\"state\": \"\"}"))
        latch.withLock {
            condition.await(5L, TimeUnit.SECONDS)
        }
        null
    }

    init {
        scope.launch {
            bus.handle<MqttMessageEvent> { ev ->
                val parts = ev.topic.split('/')

                // Check prefix and device id match
                if (parts.size < 2 || parts[0] != prefix || parts[1] != deviceId) return@handle

                // Partial update, ignore for now
                if (parts.size > 2) return@handle

                latestUpdate = decode(ev.payload)
                callback()
                latch.withLock {
                    condition.signalAll()
                }
            }
        }
    }

    fun get() = try {
        latestUpdate
    } catch (e: Exception) {
        throw GetStateException(GoogleHomeErrorCode.TransientError)
    }

    companion object {
        private val threadPool = newFixedThreadPoolContext(2, "Zigbee2MqttHelper")
        val scope = CoroutineScope(threadPool)
    }
}
