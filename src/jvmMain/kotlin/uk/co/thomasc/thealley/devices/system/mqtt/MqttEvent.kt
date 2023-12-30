package uk.co.thomasc.thealley.devices.system.mqtt

import uk.co.thomasc.thealley.devices.IAlleyEvent

abstract class MqttEvent : IAlleyEvent {
    abstract val topic: String
    abstract val payload: String
}

data class MqttMessageEvent(override val topic: String, override val payload: String) : MqttEvent()
data class MqttSendEvent(override val topic: String, override val payload: String) : MqttEvent()
