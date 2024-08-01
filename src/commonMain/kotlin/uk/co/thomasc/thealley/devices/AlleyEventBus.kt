package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.system.IAlleyEvent
import kotlin.reflect.KClass

abstract class AlleyEventBus : AlleyEventEmitter {
    abstract suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>)
    abstract override suspend fun <T : IAlleyEvent> emit(event: T)

    abstract suspend fun <T : IAlleyEvent> remove(clazz: KClass<T>, handler: EventHandler<T>)
}
