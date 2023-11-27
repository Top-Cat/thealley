package uk.co.thomasc.thealley.devicev2

import kotlin.reflect.KClass

fun interface EventHandler<T> {
    suspend fun invoke(event: T)
}

abstract class AlleyEventBus {
    suspend inline fun <reified T : IAlleyEvent> handle(handler: EventHandler<T>) =
        handle(T::class, handler)

    abstract suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>)
    abstract suspend fun <T : IAlleyEvent> emit(event: T)
}
