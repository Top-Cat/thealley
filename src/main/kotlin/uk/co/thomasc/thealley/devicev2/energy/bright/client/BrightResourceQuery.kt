package uk.co.thomasc.thealley.devicev2.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightResourceQuery(
    val from: kotlinx.datetime.LocalDateTime,
    val to: kotlinx.datetime.LocalDateTime,
    val period: BrightPeriod,
    val function: BrightFunction
)
