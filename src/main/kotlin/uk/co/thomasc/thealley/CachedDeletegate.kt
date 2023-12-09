package uk.co.thomasc.thealley

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun <T> cached(interval: Duration, block: suspend () -> T?) = CachedDelegate(interval, block)
class CachedDelegate<T>(private val interval: Duration, private val block: suspend () -> T?) {
    private var nextRequest = Instant.DISTANT_PAST
    private var cachedValue: T? = null
    private val mutex = Mutex()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        runBlocking(Dispatchers.Default) {
            mutex.withLock {
                if (nextRequest < Clock.System.now()) {
                    setValue(thisRef, property, block())
                }
            }
        }
        return cachedValue ?: throw Exception("Unable to retrieve uk.co.thomasc.thealley.cached value")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value != null) {
            cachedValue = value
            nextRequest = Clock.System.now().plus(interval)
        }
    }
}
