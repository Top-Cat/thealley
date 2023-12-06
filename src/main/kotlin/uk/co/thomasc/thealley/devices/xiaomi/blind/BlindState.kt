package uk.co.thomasc.thealley.devices.xiaomi.blind

import kotlinx.serialization.Serializable

@Serializable
data class BlindState(val position: Int? = null)
