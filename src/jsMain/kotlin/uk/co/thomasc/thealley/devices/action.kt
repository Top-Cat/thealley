package uk.co.thomasc.thealley.devices

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.dom.div
import react.dom.input
import react.dom.option
import react.dom.select
import react.fc
import uk.co.thomasc.thealley.devices.system.conditional.actions.IConditionAction
import uk.co.thomasc.thealley.devices.system.conditional.actions.RelayConditionAction
import uk.co.thomasc.thealley.devices.system.conditional.actions.SceneConditionAction
import uk.co.thomasc.thealley.devices.types.IAlleyRelayConfig
import uk.co.thomasc.thealley.devices.types.SceneConfig

external interface ConditionActionEditorProps : Props {
    var fieldName: String
    var deviceLookup: Map<Int, AlleyDeviceConfig>
    var action: IConditionAction?
    var updateAction: (IConditionAction) -> Unit
}

val conditionActionEditor = fc<ConditionActionEditorProps> { props ->
    val conditionCreator = listOf(
        RelayConditionAction::class to { RelayConditionAction(0, false) },
        SceneConditionAction::class to { SceneConditionAction(0) }
    )

    select {
        attrs.onChangeFunction = {
            val elem = it.currentTarget as HTMLSelectElement
            val cond = conditionCreator[elem.value.toIntOrNull() ?: 0]
            props.updateAction(cond.second())
        }

        conditionCreator.forEachIndexed { idx, (actionType, creator) ->
            option {
                attrs.value = idx.toString()
                attrs.selected = actionType.isInstance(props.action)
                +"${actionType.simpleName}"
            }
        }
    }

    when (val action = props.action) {
        is RelayConditionAction ->
            div {
                devicePicker {
                    attrs.fieldName = props.fieldName
                    attrs.deviceLookup = props.deviceLookup.filter {
                        it.value.config is IAlleyRelayConfig
                    }
                    attrs.deviceIds = listOf(action.deviceId)
                    attrs.updateDeviceIds = {
                        props.updateAction(action.copy(deviceId = it.first()))
                    }
                    attrs.changeSize = false
                }
                input(InputType.checkBox) {
                    attrs.id = "relay-action-${props.fieldName}"
                    attrs.checked = action.state
                    attrs.onChangeFunction = { ev ->
                        props.updateAction(action.copy(state = (ev.currentTarget as HTMLInputElement).checked))
                    }
                }
            }
        is SceneConditionAction ->
            devicePicker {
                attrs.fieldName = props.fieldName
                attrs.deviceLookup = props.deviceLookup.filter {
                    it.value.config is SceneConfig
                }
                attrs.deviceIds = listOf(action.sceneId)
                attrs.updateDeviceIds = {
                    props.updateAction(action.copy(sceneId = it.first()))
                }
                attrs.changeSize = false
            }
        null ->
            div {
                +"TODO: Is this possible?"
            }
    }
}
