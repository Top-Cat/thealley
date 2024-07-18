package uk.co.thomasc.thealley.devices

import kotlinx.html.js.onClickFunction
import react.Props
import react.dom.button
import react.dom.div
import react.dom.i
import react.dom.p
import react.fc
import uk.co.thomasc.thealley.devices.types.IAlleyRelayConfig
import uk.co.thomasc.thealley.devices.types.SceneConfig

external interface SceneEditorProps : Props {
    var fieldName: String
    var deviceLookup: Map<Int, AlleyDeviceConfig>
    var elements: List<SceneConfig.ScenePart>
    var updateElements: (List<SceneConfig.ScenePart>) -> Unit
}

val sceneEditor = fc<SceneEditorProps> { props ->
    props.elements.forEachIndexed { idx, elem ->
        div("picker-element") {
            devicePicker {
                attrs.fieldName = props.fieldName
                attrs.deviceLookup = props.deviceLookup.filter {
                    it.value.config is IAlleyRelayConfig
                }
                attrs.deviceIds = listOf(elem.lightId)
                attrs.updateDeviceIds = {
                    val newElem = elem.copy(lightId = it.first())
                    props.updateElements(props.elements.updated(idx, newElem))
                }
                attrs.changeSize = false
            }

            // TODO: Allow editing
            p {
                +"(H: ${elem.hue}, S: ${elem.saturation}, V: ${elem.brightness}) ${elem.temperature}K"
            }

            button(classes = "rem") {
                attrs.onClickFunction = {
                    props.updateElements(props.elements.minus(elem))
                }
                i("fas fa-times-circle") {}
            }
        }
    }

    // TODO: Add new element
}
