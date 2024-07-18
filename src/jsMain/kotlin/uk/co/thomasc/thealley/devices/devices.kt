package uk.co.thomasc.thealley.devices

import external.axiosGet
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.defaultValue
import react.dom.div
import react.dom.input
import react.dom.label
import react.dom.option
import react.dom.p
import react.dom.select
import react.fc
import react.useEffect
import react.useEffectOnce
import react.useState
import uk.co.thomasc.thealley.devices.types.ConditionActionConfigField
import uk.co.thomasc.thealley.devices.types.ConditionConfigField
import uk.co.thomasc.thealley.devices.types.ConditionListConfigField
import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import uk.co.thomasc.thealley.devices.types.SceneElementConfigField
import uk.co.thomasc.thealley.devices.types.ScheduleElementConfigField
import kotlin.time.Duration.Companion.seconds

val devicesDialog = fc<Props> { _ ->
    val (devices, setDevices) = useState(emptyList<AlleyDeviceConfig>())
    val (deviceLookup, setDeviceLookup) = useState(emptyMap<Int, AlleyDeviceConfig>())
    val (device, setDevice) = useState<AlleyDeviceConfig>()
    val (newConfig, setNewConfig) = useState<IAlleyConfig>()

    useEffectOnce {
        axiosGet<List<AlleyDeviceConfig>>("/api/devices").then {
            setDevices(it)
        }
    }

    useEffect(devices) {
        val dev = devices.firstOrNull()
        setDevice(dev)
        setNewConfig(dev?.config)
        setDeviceLookup(devices.associateBy { it.id })
    }

    useEffect(newConfig) {
        console.log("NEW CONFIG", newConfig)
    }

    div {
        attrs.id = "devices"
        div {
            select {
                attrs.value = device?.id.toString()
                attrs.onChangeFunction = { ev ->
                    val dev = devices.first { it.id.toString() == (ev.target as HTMLSelectElement).value }
                    setDevice(dev)
                    setNewConfig(dev.config)
                }

                devices.forEach {
                    option {
                        attrs.value = it.id.toString()
                        +"${it.config.name} (${it.config::class.simpleName})"
                    }
                }
            }

            if (newConfig != null) {
                device?.let { d ->
                    div("device") {
                        p { +"Id: ${d.id}" }

                        if (d.config is IConfigEditable<*>) {
                            d.config.fields.forEach { field ->
                                div {
                                    label {
                                        attrs.htmlFor = field.name
                                        +field.name
                                    }

                                    when (field) {
                                        is StringConfigField<*> -> {
                                            input(InputType.text) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.defaultValue = field.get(newConfig) ?: ""
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, (ev.currentTarget as HTMLInputElement).value))
                                                }
                                            }
                                        }

                                        is PasswordConfigField<*> -> {
                                            input(InputType.password) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.defaultValue = field.get(newConfig) ?: ""
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, (ev.currentTarget as HTMLInputElement).value))
                                                }
                                            }
                                        }

                                        is DurationConfigField<*> -> {
                                            input(InputType.number) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.defaultValue = field.get(newConfig)?.inWholeSeconds.toString()
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, ((ev.currentTarget as HTMLInputElement).value.toIntOrNull() ?: 0).seconds))
                                                }
                                            }
                                        }

                                        is IntConfigField<*> -> {
                                            input(InputType.number) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.defaultValue = field.get(newConfig).toString()
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, (ev.currentTarget as HTMLInputElement).value.toIntOrNull() ?: 0))
                                                }
                                            }
                                        }

                                        is DoubleConfigField<*> -> {
                                            input(InputType.number) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.defaultValue = field.get(newConfig).toString()
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, (ev.currentTarget as HTMLInputElement).value.toDoubleOrNull() ?: 0.0))
                                                }
                                            }
                                        }

                                        is BooleanConfigField<*> -> {
                                            input(InputType.checkBox) {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.id = field.name
                                                attrs.checked = field.get(newConfig) == true
                                                attrs.onChangeFunction = { ev ->
                                                    setNewConfig(field.set(newConfig, (ev.currentTarget as HTMLInputElement).checked))
                                                }
                                            }
                                        }

                                        is DeviceConfigField<*> -> {
                                            devicePicker {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup.filter {
                                                    field.filter(it.value.config)
                                                }
                                                attrs.deviceIds = listOf(field.get(newConfig) ?: 0)
                                                attrs.updateDeviceIds = {
                                                    setNewConfig(field.set(newConfig, it.first()))
                                                }
                                                attrs.changeSize = false
                                            }
                                        }

                                        is DeviceListConfigField<*> -> {
                                            devicePicker {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup.filter {
                                                    field.filter(it.value.config)
                                                }
                                                attrs.deviceIds = field.get(newConfig) ?: listOf()
                                                attrs.updateDeviceIds = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                                attrs.changeSize = true
                                            }
                                        }

                                        is ScheduleElementConfigField -> {
                                            scheduleEditor {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.elements = field.get(newConfig) ?: listOf()
                                                attrs.updateElements = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                            }
                                        }

                                        is SceneElementConfigField -> {
                                            sceneEditor {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup
                                                attrs.elements = field.get(newConfig) ?: listOf()
                                                attrs.updateElements = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                            }
                                        }

                                        is ConditionActionConfigField -> {
                                            conditionActionEditor {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup
                                                attrs.action = field.get(newConfig)
                                                attrs.updateAction = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                            }
                                        }

                                        is ConditionConfigField -> {
                                            conditionEditor {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup
                                                attrs.condition = field.get(newConfig)
                                                attrs.updateCondition = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                            }
                                        }

                                        is ConditionListConfigField -> {
                                            conditionListEditor {
                                                key = "field-${d.id}-${field.name}"
                                                attrs.fieldName = field.name
                                                attrs.deviceLookup = deviceLookup
                                                attrs.conditions = field.get(newConfig) ?: listOf()
                                                attrs.updateConditions = {
                                                    setNewConfig(field.set(newConfig, it))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
