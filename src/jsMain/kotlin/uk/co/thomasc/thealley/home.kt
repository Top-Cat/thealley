package uk.co.thomasc.thealley

import external.axiosGet
import react.Props
import react.fc
import react.useEffect
import react.useEffectOnce
import react.useState
import uk.co.thomasc.thealley.web.BulbState
import uk.co.thomasc.thealley.web.DeviceInfo

val home = fc<Props> {
    val (lights, setLights) = useState<List<DeviceInfo>>()
    val (lightStateMap, setLightStateMap) = useState(mapOf<Int, BulbState>())
    val (selectedLight, setSelectedLight) = useState<DeviceInfo>()

    useEffectOnce {
        axiosGet<List<DeviceInfo>>("/control/list").then {
            setLights(it.data)
        }
    }

    useEffect(lights) {
        if (lights == null) return@useEffect

        val ids = lights.map { it.id }.joinToString(",")
        axiosGet<Map<Int, BulbState>>("/control/multi/$ids").then {
            setLightStateMap(lightStateMap.plus(it.data))
        }
    }

    if (selectedLight != null) {
        dialog {
            attrs.light = selectedLight
            attrs.state = lightStateMap[selectedLight.id] ?: BulbState(false)
            attrs.close = {
                setSelectedLight(null)
            }
            attrs.updateState = {
                setLightStateMap(lightStateMap.plus(selectedLight.id to it))
            }
        }
    }

    lights?.forEach { light ->
        lightControl {
            attrs.light = light
            attrs.state = lightStateMap[light.id]
            attrs.showDialog = {
                setSelectedLight(light)
            }
        }
    }
}
