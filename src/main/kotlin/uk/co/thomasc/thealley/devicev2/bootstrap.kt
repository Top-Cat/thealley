package uk.co.thomasc.thealley.devicev2

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

private class AlleyEventBusImpl : AlleyEventBus() {
    val listeners = mutableMapOf<KClass<out IAlleyEvent>, MutableList<EventHandler<*>>>()

    override suspend fun <T : IAlleyEvent> handle(clazz: KClass<T>, handler: EventHandler<T>) {
        listeners.getOrPut(clazz) { mutableListOf() }.add(handler)
    }

    override suspend fun <T : IAlleyEvent> emit(event: T) {
        listeners[event::class]?.filterIsInstance<EventHandler<T>>()?.forEach {
            it.invoke(event)
        }
    }
}

class StateUpdaterFactory(private val json: Json, private val id: Int) {
    fun <U> getUpdater(serializer: KSerializer<U>): IStateUpdater<U> = StateUpdaterImpl(json, serializer, id)
}

private class StateUpdaterImpl<U>(val json: Json, val serializer: KSerializer<U>, val id: Int) : IStateUpdater<U> {
    override fun saveState(state: U) {
        val encoded = json.encodeToString(serializer, state)
        println("Update state for $id - $encoded")
    }
}

suspend fun initDevice(device: AlleyDevice<*, *, *>, bus: AlleyEventBus) {
    device.init(bus)
}

fun newDevices() {
    val json = Json {
        prettyPrint = true
    }

    val devices = listOf(
        SunConfig(53.8076891, -1.5979767, "UTC") to "{\"daytime\": false}",
        RelayConfig("RELAY1", "612E36334A7A3CD6") to "{\"on\": false}"
    )

    val bus = AlleyEventBusImpl()
    runBlocking {
        devices.map {
            val stateFactory = StateUpdaterFactory(json, 1)
            it.first.deviceConfig().generate(json, stateFactory, it.second)
        }.forEach {
            initDevice(it, bus)
        }

        while(true) {
            delay(1000)
            bus.emit(TickEvent)
        }
    }
}
