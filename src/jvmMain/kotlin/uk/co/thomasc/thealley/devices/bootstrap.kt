package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import mu.KLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.types.deviceConfig
import uk.co.thomasc.thealley.repo.DeviceDao
import uk.co.thomasc.thealley.repo.DeviceTable
import uk.co.thomasc.thealley.repo.NowExpression
import kotlin.reflect.KClass

private class AlleyEventBusImpl : AlleyEventBus() {
    val threadPool = newFixedThreadPoolContext(4, "AlleyEventBus")
    val channel = Channel<suspend () -> Unit>(50)
    val listeners = mutableMapOf<KClass<out IAlleyEvent>, MutableList<EventHandler<*>>>()

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
            logger.info { "Emitting event $event" }
        }

        channel.send {
            listeners[event::class]?.filterIsInstance<EventHandler<T>>()?.forEach {
                it.invoke(event)
            }
        }
    }

    companion object : KLogging()
}

private class AlleyDeviceMapperImpl : AlleyDeviceMapper() {
    private val deviceList = mutableListOf<AlleyDevice<*, *, *>>()

    override suspend fun <T : AlleyDevice<*, *, *>> getDevice(id: Int, clazz: KClass<T>) =
        getDevices(clazz).firstOrNull { it.id == id }

    override suspend fun <T : AlleyDevice<*, *, *>> getDevices(clazz: KClass<T>): List<T> =
        deviceList.filterIsInstance(clazz.java)

    override suspend fun register(device: AlleyDevice<*, *, *>) {
        deviceList.add(device)
    }

    override suspend fun deregister(device: AlleyDevice<*, *, *>) {
        deviceList.remove(device)
    }
}

class StateUpdaterFactory(private val json: Json, private val id: Int) {
    fun <U> getUpdater(serializer: KSerializer<U>): IStateUpdater<U> = StateUpdaterImpl(json, serializer, id)
}

private class StateUpdaterImpl<U>(val json: Json, val serializer: KSerializer<U>, val id: Int) : IStateUpdater<U> {
    override suspend fun saveState(newState: U) {
        val localId = id
        val encoded = json.encodeToString(serializer, newState)
        logger.info { "Update state for $localId - $encoded" }

        newSuspendedTransaction {
            DeviceTable.update({
                DeviceTable.id eq localId
            }) {
                it[state] = encoded
                it[updatedAt] = NowExpression(updatedAt.columnType)
            }
        }
    }

    companion object : KLogging()
}

suspend fun initDevice(device: AlleyDevice<*, *, *>, bus: AlleyEventBus) {
    device.init(bus)
}

fun newDevices(): Pair<AlleyEventBus, AlleyDeviceMapper> {
    val json = Json {
        prettyPrint = false
    }

    val devices = transaction {
        DeviceDao.wrapRows(
            DeviceTable.select {
                DeviceTable.enabled eq true
            }
        ).map { Triple(it.id.value, it.config, it.state) }
    }

    val bus = AlleyEventBusImpl()
    return bus to AlleyDeviceMapperImpl().also { dm ->
        devices.map { (id, config, state) ->
            val stateFactory = StateUpdaterFactory(json, id)
            config.deviceConfig().generate(id, json, stateFactory, state, dm)
        }.also { deviceList ->
            GlobalScope.launch {
                deviceList.forEach {
                    dm.register(it)
                }

                deviceList.forEach {
                    withTimeout(1000) {
                        initDevice(it, bus)
                    }
                }

                bus.start()

                while (true) {
                    delay(10 * 1000)
                    bus.emit(TickEvent)
                }
            }
        }
    }
}
