package uk.co.thomasc.thealley.devicev2.xiaomi.blind

import kotlinx.serialization.Serializable

@Serializable
data class BlindState(val position: Int? = null)
