package uk.co.thomasc.thealley.devices.state.switch

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

// TODO: Delete this

@Serializable
data class SwitchState(val state: Int = 0) : IAlleyState
