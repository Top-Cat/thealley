package uk.co.thomasc.thealley.devices

import react.Props
import react.fc
import uk.co.thomasc.thealley.devices.system.conditional.conditions.ICondition

external interface ConditionListEditorProps : Props {
    var fieldName: String
    var deviceLookup: Map<Int, AlleyDeviceConfig>
    var conditions: List<ICondition>
    var updateConditions: (List<ICondition>) -> Unit
}

fun <E> Iterable<E>.updated(index: Int, elem: E) = mapIndexed { i, existing -> if (i == index) elem else existing }

val conditionListEditor = fc<ConditionListEditorProps> { props ->
    props.conditions.forEachIndexed { idx, cond ->
        conditionEditor {
            attrs.fieldName = props.fieldName
            attrs.deviceLookup = props.deviceLookup
            attrs.condition = cond
            attrs.updateCondition = { newCond ->
                props.updateConditions(props.conditions.updated(idx, newCond))
            }
        }

        // TODO: Remove condition
    }

    // TODO: Add new element
}
