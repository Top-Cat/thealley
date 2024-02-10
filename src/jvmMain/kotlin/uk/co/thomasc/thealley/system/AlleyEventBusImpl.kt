package uk.co.thomasc.thealley.system

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import mu.KLogging
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EventHandler
import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import kotlin.reflect.KClass

internal class AlleyEventBusImpl : AlleyEventBus() {
    private val threadPool = newFixedThreadPoolContext(4, "AlleyEventBus")
    private val channel = Channel<suspend () -> Unit>(50)
    private val listeners = mutableMapOf<KClass<out IAlleyEvent>, MutableList<EventHandler<*>>>()

    fun start() {
        repeat(10) {
            GlobalScope.launch(threadPool) {
                while (true) {
                    channel.receive().invoke()
                }
            }
        }
    }

    override suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>) {
        listeners.getOrPut(clazz) { mutableListOf() }.add(handler)
    }

    override suspend fun <T : IAlleyEvent> emit(event: T) {
        if (event !is MqttMessageEvent && event !is TickEvent && event !is ReportStateEvent) {
            logger.debug { "Emitting event $event" }
        }

        channel.send {
            try {
                listeners[event::class]?.filterIsInstance<EventHandler<T>>()?.forEach {
                    it.invoke(event)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error handing ${event.javaClass.simpleName}" }
            }
        }
    }

    companion object : KLogging()
}
