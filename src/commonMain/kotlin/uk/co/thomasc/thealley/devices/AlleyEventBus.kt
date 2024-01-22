package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import kotlin.reflect.KClass

abstract class AlleyEventBus : AlleyEventEmitter {
    suspend inline fun <reified T : IAlleyEvent> handle(handler: EventHandler<T>) =
        handle(T::class, handler)

    abstract suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>)
    abstract override suspend fun <T : IAlleyEvent> emit(event: T)
}
