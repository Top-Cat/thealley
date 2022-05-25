package uk.co.thomasc.thealley.rest

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import uk.co.thomasc.thealley.scenes.SceneController

sealed class EventData(open val sensor: String)
data class MotionData(override val sensor: String) : EventData(sensor)
data class PropertyData(override val sensor: String, val property: String, val value: Double) : EventData(sensor)

@Location("/api")
class ApiRoute {
    @Location("/motion")
    data class Motion(val api: ApiRoute)
    @Location("/prop")
    data class Prop(val api: ApiRoute)
}

class Api {
    val sensorData = mutableMapOf<String, MutableMap<String, Double>>()

    fun onPropertyChange(obj: PropertyData) {
        sensorData.getOrElse(obj.sensor) {
            val newObj = mutableMapOf<String, Double>()
            sensorData[obj.sensor] = newObj
            newObj
        }.let {
            it[obj.property] = obj.value
        }
    }
}

fun Route.apiRoute(api: Api, sceneController: SceneController) {
    post<ApiRoute.Motion> {
        val obj = call.receive<MotionData>()
        sceneController.onChange(obj.sensor)
    }

    post<ApiRoute.Prop> {
        val obj = call.receive<PropertyData>()
        api.onPropertyChange(obj)
    }

    get<ApiRoute.Prop> {
        call.respond(api.sensorData)
    }
}
