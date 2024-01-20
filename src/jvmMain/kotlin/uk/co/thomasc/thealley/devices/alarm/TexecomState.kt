package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.Serializable

@Serializable
data class TexecomState(
    val armLevel: GoogleArmLevel = GoogleArmLevel.NONE
)
