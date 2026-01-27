package uk.co.thomasc.thealley.devices.leeds.bin

import kotlinx.datetime.LocalDate
import uk.co.thomasc.thealley.devices.system.IAlleyEvent

data class LeedsBinEvent(val nextBlack: LocalDate?, val nextGreen: LocalDate?, val nextBrown: LocalDate?) : IAlleyEvent
