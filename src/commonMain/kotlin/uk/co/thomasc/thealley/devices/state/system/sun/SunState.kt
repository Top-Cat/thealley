package uk.co.thomasc.thealley.devices.state.system.sun

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class SunState(val daytime: Boolean) : IAlleyState
