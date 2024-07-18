package uk.co.thomasc.thealley.devices

import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.button
import react.dom.div
import react.dom.i
import react.dom.option
import react.dom.select
import react.fc
import react.useEffect
import react.useRef
import react.useState

external interface DevicePickerProps : Props {
    var fieldName: String
    var deviceLookup: Map<Int, AlleyDeviceConfig>
    var deviceIds: List<Int>
    var updateDeviceIds: (List<Int>) -> Unit
    var changeSize: Boolean
}

val devicePicker = fc<DevicePickerProps> { props ->
    fun filteredLookup(includeId: Int? = null) = props.deviceLookup.filter { it.key == includeId || !props.deviceIds.contains(it.key) }

    val dropdownRef = useRef<HTMLSelectElement>()
    val (selectedId, setSelectedId) = useState(0)

    useEffect(props.deviceIds) {
        setSelectedId(dropdownRef.current?.value?.toIntOrNull() ?: 0)
    }

    props.deviceIds.forEachIndexed { idx, id ->
        div("picker-element") {
            select {
                ref = dropdownRef
                attrs.id = "select-${props.fieldName}-$idx"
                attrs.onChangeFunction = { ev ->
                    val newId = (ev.currentTarget as HTMLSelectElement).value.toIntOrNull() ?: 0
                    props.updateDeviceIds(props.deviceIds.updated(idx, newId))
                }

                filteredLookup(id).values.forEach {
                    option {
                        attrs.value = it.id.toString()
                        attrs.selected = it.id == id
                        +"${it.config.name} (${it.config::class.simpleName})"
                    }
                }
            }

            if (props.changeSize) {
                button(classes = "rem") {
                    attrs.onClickFunction = {
                        props.updateDeviceIds(props.deviceIds.minus(id))
                    }
                    i("fas fa-times-circle") {}
                }
            }
        }
    }

    if (props.changeSize) {
        div("picker-element") {
            select {
                ref = dropdownRef
                attrs.id = props.fieldName
                attrs.onChangeFunction = { ev ->
                    val newId = (ev.currentTarget as HTMLSelectElement).value.toIntOrNull() ?: 0
                    setSelectedId(newId)
                }

                filteredLookup().values.forEach {
                    option {
                        attrs.value = it.id.toString()
                        +"${it.config.name} (${it.config::class.simpleName})"
                    }
                }
            }

            button(classes = "add") {
                attrs.onClickFunction = {
                    props.updateDeviceIds(props.deviceIds.plus(selectedId))
                }
                i("fas fa-plus-circle") {}
            }
        }
    }
}
