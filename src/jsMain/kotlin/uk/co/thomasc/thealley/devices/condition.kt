package uk.co.thomasc.thealley.devices

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.defaultValue
import react.dom.div
import react.dom.input
import react.dom.option
import react.dom.select
import react.fc
import uk.co.thomasc.thealley.devices.alarm.TexecomAreaStatus
import uk.co.thomasc.thealley.devices.alarm.ZoneState
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition
import uk.co.thomasc.thealley.devices.system.conditional.conditions.RelayCondition
import uk.co.thomasc.thealley.devices.system.conditional.conditions.SunCondition
import uk.co.thomasc.thealley.devices.system.conditional.conditions.TexecomAreaCondition
import uk.co.thomasc.thealley.devices.system.conditional.conditions.TexecomZoneCondition
import uk.co.thomasc.thealley.devices.types.IAlleyRelayConfig

external interface ConditionEditorProps : Props {
    var fieldName: String
    var deviceLookup: Map<Int, AlleyDeviceConfig>
    var condition: ICondition?
    var updateCondition: (ICondition) -> Unit
}

val conditionEditor = fc<ConditionEditorProps> { props ->
    val conditionCreator = listOf(
        RelayCondition::class to { RelayCondition(0, true) },
        SunCondition::class to { SunCondition(true) },
        TexecomAreaCondition::class to { TexecomAreaCondition(0, TexecomAreaStatus.ARMED) },
        TexecomZoneCondition::class to { TexecomZoneCondition(0, ZoneState.Active) }
    )

    select {
        attrs.onChangeFunction = {
            val elem = it.currentTarget as HTMLSelectElement
            val cond = conditionCreator[elem.value.toIntOrNull() ?: 0]
            props.updateCondition(cond.second())
        }

        conditionCreator.forEachIndexed { idx, (actionType, _) ->
            option {
                attrs.value = idx.toString()
                attrs.selected = actionType.isInstance(props.condition)
                +"${actionType.simpleName}"
            }
        }
    }

    when (val condition = props.condition) {
        is RelayCondition ->
            div {
                devicePicker {
                    attrs.fieldName = props.fieldName
                    attrs.deviceLookup = props.deviceLookup.filter {
                        it.value.config is IAlleyRelayConfig
                    }
                    attrs.deviceIds = listOf(condition.deviceId)
                    attrs.updateDeviceIds = {
                        props.updateCondition(condition.copy(deviceId = it.first()))
                    }
                    attrs.changeSize = false
                }
                input(InputType.checkBox) {
                    attrs.id = "relay-action-${props.fieldName}"
                    attrs.checked = condition.state
                    attrs.onChangeFunction = { ev ->
                        props.updateCondition(condition.copy(state = (ev.currentTarget as HTMLInputElement).checked))
                    }
                }
            }
        is SunCondition ->
            input(InputType.checkBox) {
                attrs.id = "relay-action-${props.fieldName}"
                attrs.checked = condition.daytime
                attrs.onChangeFunction = { ev ->
                    props.updateCondition(condition.copy(daytime = (ev.currentTarget as HTMLInputElement).checked))
                }
            }
        is TexecomAreaCondition ->
            div {
                input(InputType.number) {
                    key = "texecom-${props.fieldName}"
                    attrs.defaultValue = condition.areaId.toString()
                    attrs.onChangeFunction = { ev ->
                        props.updateCondition(condition.copy(areaId = (ev.currentTarget as HTMLInputElement).value.toIntOrNull() ?: 0))
                    }
                }
                select {
                    attrs.onChangeFunction = { ev ->
                        val newStatus = TexecomAreaStatus.valueOf((ev.currentTarget as HTMLSelectElement).value)
                        props.updateCondition(condition.copy(areaState = newStatus))
                    }
                    TexecomAreaStatus.entries.forEach { areaStatus ->
                        option {
                            attrs.selected = condition.areaState == areaStatus
                            +areaStatus.name
                        }
                    }
                }
            }
        is TexecomZoneCondition ->
            div {
                input(InputType.number) {
                    key = "texecom-${props.fieldName}"
                    attrs.defaultValue = condition.zoneId.toString()
                    attrs.onChangeFunction = { ev ->
                        props.updateCondition(condition.copy(zoneId = (ev.currentTarget as HTMLInputElement).value.toIntOrNull() ?: 0))
                    }
                }
                select {
                    attrs.onChangeFunction = { ev ->
                        val newStatus = ZoneState.valueOf((ev.currentTarget as HTMLSelectElement).value)
                        props.updateCondition(condition.copy(zoneState = newStatus))
                    }
                    ZoneState.entries.forEach { zoneStatus ->
                        option {
                            attrs.selected = condition.zoneState == zoneStatus
                            +zoneStatus.name
                        }
                    }
                }
            }
        null ->
            div {
                +"TODO: Is this possible?"
            }
    }
}
