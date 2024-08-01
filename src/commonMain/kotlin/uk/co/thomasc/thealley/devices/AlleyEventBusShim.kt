package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import kotlin.reflect.KClass

class AlleyEventBusShim(val bus: AlleyEventBus) : AlleyEventEmitter {
    private data class Registration<T : IAlleyEvent>(val clazz: KClass<T>, val handler: EventHandler<T>) {
        suspend fun close(bus: AlleyEventBus) {
            bus.remove(clazz, handler)
        }
    }

    private var closed: Boolean = false
    private val handlers: MutableList<Registration<*>> = mutableListOf()

    suspend inline fun <reified T : IAlleyEvent> handle(handler: EventHandler<T>) =
        handle(T::class, handler)

    suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>) {
        if (closed) return

        bus.handle(clazz, handler).also {
            handlers.add(Registration(clazz, handler))
        }
    }

    suspend fun close() {
        if (closed) return
        closed = true

        handlers.forEach {
            it.close(bus)
        }
    }

    override suspend fun <T : IAlleyEvent> emit(event: T) = bus.emit(event)
}
