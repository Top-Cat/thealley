package uk.co.thomasc.thealley.devices.state.leeds.bin

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class LeedsBinState(val nextCatchup: Instant? = null, val nextBlack: LocalDate? = null, val nextGreen: LocalDate? = null, val nextBrown: LocalDate? = null) : IAlleyState
