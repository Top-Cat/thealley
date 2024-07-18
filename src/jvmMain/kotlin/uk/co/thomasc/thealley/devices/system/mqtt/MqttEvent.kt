package uk.co.thomasc.thealley.devices.system.mqtt

import kotlinx.serialization.encodeToString
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.system.IAlleyEvent

abstract class MqttEvent : IAlleyEvent {
    abstract val topic: String
    abstract val payload: String
}

data class MqttMessageEvent(override val topic: String, override val payload: String) : MqttEvent()
data class MqttSendEvent(override val topic: String, override val payload: String) : MqttEvent() {
    companion object {
        inline fun <reified T> from(topic: String, payload: T) =
            MqttSendEvent(topic, alleyJsonUgly.encodeToString(payload))
    }
}
