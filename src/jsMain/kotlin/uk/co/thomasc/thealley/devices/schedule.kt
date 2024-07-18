package uk.co.thomasc.thealley.devices

import kotlinx.html.js.onClickFunction
import react.Props
import react.dom.button
import react.dom.div
import react.dom.i
import react.dom.p
import react.fc
import uk.co.thomasc.thealley.devices.types.ScheduleConfig

external interface ScheduleEditorProps : Props {
    var fieldName: String
    var elements: List<ScheduleConfig.ScheduleElement>
    var updateElements: (List<ScheduleConfig.ScheduleElement>) -> Unit
}

val scheduleEditor = fc<ScheduleEditorProps> { props ->
    props.elements.forEach { elem ->
        div("picker-element") {
            // TODO: Allow editing
            p {
                +"${elem.time} -> ${elem.state}"
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
