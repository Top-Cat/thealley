package uk.co.thomasc.thealley.devices

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.dom.button
import react.dom.defaultValue
import react.dom.div
import react.dom.i
import react.dom.input
import react.fc
import react.useState

external interface StringListEditorProps : Props {
    var fieldName: String
    var changeSize: Boolean
    var strings: List<String>
    var updateStrings: (List<String>) -> Unit
}

val stringListEditor = fc<StringListEditorProps> { props ->
    val (newValue, setNewValue) = useState("")

    props.strings.forEachIndexed { idx, str ->
        div("picker-element") {
            input(InputType.text) {
                attrs.defaultValue = str
                attrs.onChangeFunction = { ev ->
                    props.updateStrings(props.strings.updated(idx, (ev.currentTarget as HTMLInputElement).value))
                }
            }

            if (props.changeSize) {
                button(classes = "rem") {
                    attrs.onClickFunction = {
                        props.updateStrings(props.strings.take(idx) + props.strings.drop(idx + 1))
                    }
                    i("fas fa-times-circle") {}
                }
            }
        }
    }

    if (props.changeSize) {
        div("picker-element") {
            input(InputType.text) {
                attrs.value = newValue
                attrs.onChangeFunction = { ev ->
                    setNewValue((ev.currentTarget as HTMLInputElement).value)
                }
            }
            button(classes = "add") {
                attrs.onClickFunction = {
                    props.updateStrings(props.strings.plus(newValue))
                    setNewValue("")
                }
                i("fas fa-plus-circle") {}
            }
        }
    }
}
