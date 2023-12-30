package uk.co.thomasc.thealley

import kotlinx.html.js.onClickFunction
import react.Props
import react.dom.div
import react.dom.jsStyle
import react.dom.span
import react.fc
import uk.co.thomasc.thealley.web.BulbState
import uk.co.thomasc.thealley.web.DeviceInfo

external interface LightProps : Props {
    var light: DeviceInfo
    var state: BulbState?
    var showDialog: () -> Unit
}

val lightControl = fc<LightProps> { props ->
    div("far") {
        attrs.jsStyle {
            color = if (props.state?.state?.let { it > 0 } == true) "#5d5" else "#888"
        }
        attrs.onClickFunction = {
            props.showDialog()
        }

        span {
            +"\uF0EB"
        }

        +props.light.config.name
    }
}
