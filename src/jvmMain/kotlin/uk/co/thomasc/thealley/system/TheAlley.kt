package uk.co.thomasc.thealley.system

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.system.TickEvent
import uk.co.thomasc.thealley.devices.types.deviceConfig
import uk.co.thomasc.thealley.repo.DeviceDao
import uk.co.thomasc.thealley.repo.DeviceTable

object TheAlley {
    fun start(): Pair<AlleyEventBus, AlleyDeviceMapper> {
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
                val stateFactory = StateUpdaterFactory(alleyJsonUgly, id)
                config.deviceConfig().generate(id, alleyJsonUgly, stateFactory, state, dm)
            }.also { deviceList ->
                GlobalScope.launch {
                    deviceList.forEach {
                        dm.register(it)
                    }

                    deviceList.forEach {
                        withTimeout(1000) {
                            it.init(bus)
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
}
