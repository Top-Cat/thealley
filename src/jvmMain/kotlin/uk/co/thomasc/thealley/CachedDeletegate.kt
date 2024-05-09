package uk.co.thomasc.thealley

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun <T> cached(interval: Duration, returnDirty: Boolean = false, block: suspend () -> T?) = CachedDelegate(interval, returnDirty, block)
class CachedDelegate<T>(private val interval: Duration, private val returnDirty: Boolean = false, private val block: suspend () -> T?) {
    private var nextRequest = Instant.DISTANT_PAST
    private var cachedValue: T? = null
    private val mutex = Mutex()

    private suspend fun fetchValue(thisRef: Any?, property: KProperty<*>) {
        mutex.withLock {
            if (nextRequest < Clock.System.now()) {
                setValue(thisRef, property, block())
            }
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (cachedValue == null || !returnDirty) {
            runBlocking(Dispatchers.Default) {
                fetchValue(thisRef, property)
            }
        } else if (nextRequest < Clock.System.now()) {
            GlobalScope.launch(Dispatchers.Default) {
                fetchValue(thisRef, property)
            }
        }

        return cachedValue ?: throw Exception("Unable to retrieve cached value")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value != null) {
            cachedValue = value
            nextRequest = Clock.System.now().plus(interval)
        }
    }
}
