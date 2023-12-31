package uk.co.thomasc.thealley

import external.Axios
import external.generateConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.js.timers.setTimeout
import org.w3c.dom.HTMLInputElement
import react.Props
import react.dom.defaultValue
import react.dom.div
import react.dom.input
import react.dom.jsStyle
import react.dom.label
import react.fc
import react.useEffect
import react.useRef
import uk.co.thomasc.thealley.web.BulbState
import uk.co.thomasc.thealley.web.DeviceInfo
import kotlin.time.Duration.Companion.milliseconds

external interface DialogProps : Props {
    var light: DeviceInfo
    var state: BulbState
    var updateState: (BulbState) -> Unit
    var close: () -> Unit
}

val temperatureColors = listOf(
    "#ff3800", "#ff5300", "#ff6500", "#ff7300", "#ff7e00", "#ff8912", "#ff932c", "#ff9d3f", "#ffa54f", "#ffad5e", "#ffb46b", "#ffbb78", "#ffc184",
    "#ffc78f", "#ffcc99", "#ffd1a3", "#ffd5ad", "#ffd9b6", "#ffddbe", "#ffe1c6", "#ffe4ce", "#ffe8d5", "#ffebdc", "#fff5f5", "#f5f3ff", "#e6ebff",
    "#dae4ff", "#d1dfff", "#cadaff", "#c4d7ff", "#c0d4ff", "#bcd1ff", "#b8cfff", "#b3ccff", "#afc9ff", "#abc7ff", "#a9c5ff", "#a6c3ff", "#a6c3ff"
)

val dialog = fc<DialogProps> { props ->
    val lastUpdate = useRef(Instant.DISTANT_PAST)
    val updateQueued = useRef(false)
    val updateRunning = useRef(false)
    val nextState = useRef<BulbState>()

    fun runUpdate() {
        updateQueued.current = false
        updateRunning.current = true
        lastUpdate.current = Clock.System.now()

        Axios.put<BulbState>("/control/${props.light.id}", nextState.current, generateConfig<BulbState, BulbState>()).then {
            // Do nothing
        }.finally {
            if (updateQueued.current == true) {
                setTimeout(::runUpdate, 500)
            } else {
                updateRunning.current = false
            }
        }
    }

    useEffect(props.state) {
        val firstLoad = nextState.current == null
        nextState.current = props.state

        if (firstLoad) return@useEffect

        if (lastUpdate.current?.let { Clock.System.now().minus(it) < 500.milliseconds } == true) {
            updateQueued.current = true

            if (updateRunning.current != true) {
                updateRunning.current = true
                setTimeout(::runUpdate, 500)
            }
        } else if (updateRunning.current != true) {
            runUpdate()
        }
    }

    div {
        attrs.id = "control"
        attrs.onClickFunction = {
            props.close()
        }

        div {
            attrs.onClickFunction = {
                it.stopPropagation()
            }

            div("far icon") {
                attrs.onClickFunction = {
                    props.updateState(props.state.copy(state = !props.state.state))
                }
                attrs.jsStyle {
                    val state = props.state

                    if (!state.state) {
                        color = "#000"
                    } else if (state.hue != null) {
                        val brightness = ((state.brightness ?: 0) + 30) * 0.6
                        color = "hsl(${state.hue},100%,$brightness%)"
                    } else if (state.temp != null) {
                        color = temperatureColors[(state.temp / 100) - 27]
                    }
                }

                +"\uF0EB"
            }
            div("fa close") {
                attrs.onClickFunction = {
                    props.close()
                }
                +"\uF00D"
            }

            label {
                attrs.htmlFor = "name"
                +"Name"
            }
            input(InputType.text) {
                attrs.id = "name"
                attrs.name = "name"
                attrs.disabled = true
                attrs.value = props.light.config.name
            }

            label {
                attrs.htmlFor = "type"
                +"Type"
            }
            input(InputType.text) {
                attrs.id = "type"
                attrs.name = "type"
                attrs.disabled = true
                attrs.value = props.light.config::class.simpleName ?: "Unknown"
            }

            label {
                attrs.htmlFor = "brightness"
                +"Brightness"
            }
            input(InputType.range) {
                attrs.id = "brightness"
                attrs.name = "brightness"
                attrs.min = "0"
                attrs.max = "100"
                attrs.defaultValue = props.state.brightness.toString()

                attrs.onChangeFunction = {
                    val newValue = (it.target as HTMLInputElement).value.toIntOrNull() ?: 0
                    props.updateState(props.state.copy(brightness = newValue))
                }
            }

            label {
                attrs.htmlFor = "hue"
                +"Hue"
            }
            input(InputType.range) {
                attrs.id = "hue"
                attrs.name = "hue"
                attrs.min = "0"
                attrs.max = "359"
                attrs.defaultValue = (props.state.hue ?: 180).toString()

                attrs.onChangeFunction = {
                    val newValue = (it.target as HTMLInputElement).value.toIntOrNull() ?: 0
                    props.updateState(props.state.copy(hue = newValue, temp = null))
                }
            }

            label {
                attrs.htmlFor = "temp"
                +"Temperature"
            }
            input(InputType.range) {
                attrs.id = "temp"
                attrs.name = "temp"
                attrs.min = "54"
                attrs.max = "130"
                attrs.defaultValue = ((props.state.temp ?: 2700) / 50).toString()

                attrs.onChangeFunction = {
                    val newValue = (it.target as HTMLInputElement).value.toIntOrNull() ?: 0
                    props.updateState(props.state.copy(hue = null, temp = newValue * 50))
                }
            }
        }
    }
}
