package uk.co.thomasc.thealley.web

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.locations.Location
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.locations.put
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import uk.co.thomasc.thealley.alleyJsonUgly
import uk.co.thomasc.thealley.devices.AlleyDeviceConfig
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import uk.co.thomasc.thealley.devices.types.deviceConfig
import uk.co.thomasc.thealley.devices.zigbee.aq2.MotionDevice
import uk.co.thomasc.thealley.repo.DeviceTable
import uk.co.thomasc.thealley.system.StateUpdaterFactory

sealed class EventData(open val sensor: String)
data class MotionData(override val sensor: String) : EventData(sensor)
data class PropertyData(override val sensor: String, val property: String, val value: Double) : EventData(sensor)

@Location("/api")
class ApiRoute : IAlleyRoute {
    @Location("/motion")
    data class Motion(val api: ApiRoute)

    @Location("/prop")
    data class Prop(val api: ApiRoute)

    @Location("/devices")
    data class Devices(val api: ApiRoute)

    @Location("/devices/{id}")
    data class DeviceById(val id: Int, val api: ApiRoute)

    override fun Route.setup(bus: AlleyEventBus, deviceMapper: AlleyDeviceMapper) {
        post<Motion> {
            val obj = call.receive<MotionData>()
            deviceMapper
                .getDevices<MotionDevice>()
                .firstOrNull { it.config.deviceId == obj.sensor }
                ?.trigger(bus)
        }

        post<Prop> {
            val obj = call.receive<PropertyData>()
            deviceMapper.getDevices<MotionDevice>().firstOrNull {
                it.config.deviceId == obj.sensor
            }?.let {
                it.props[obj.property] = JsonPrimitive(obj.value)
            }
        }

        get<Prop> {
            val sensorData = deviceMapper.getDevices<MotionDevice>().associate {
                it.config.deviceId to it.props
            }
            call.respond(sensorData)
        }

        get<Devices> {
            val config = deviceMapper.getDevices().map { AlleyDeviceConfig(it.id, it.config) }.sortedBy { it.id }
            call.respond(config)
        }

        put<DeviceById> { req ->
            val id = req.id
            val newConfig = call.receive<IAlleyConfig>()
            val newDeviceConfig = newConfig.deviceConfig()

            deviceMapper.getDevice(id)?.let { currentDevice ->
                newSuspendedTransaction {
                    DeviceTable.update({
                        DeviceTable.id eq id
                    }) {
                        it[config] = newConfig
                    }

                    deviceMapper.deregister(currentDevice)
                    currentDevice.close()

                    val stateFactory = StateUpdaterFactory(alleyJsonUgly, id)
                    val newDevice = newDeviceConfig.generate(id, alleyJsonUgly, stateFactory, currentDevice.getStateAsString(), deviceMapper)

                    withTimeout(1000) {
                        newDevice.init(bus)
                    }

                    deviceMapper.register(newDevice)
                }
            } ?: throw BadRequestException("Device not found")

            call.respond(HttpStatusCode.OK)
        }
    }
}
