package uk.co.thomasc.thealley.devices.system.sun

import kotlinx.serialization.Serializable

@Serializable
data class SunState(val daytime: Boolean)
