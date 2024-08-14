package uk.co.thomasc.thealley.devices.state.energy.tado

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
data class TadoState(val lastMeterReadDate: LocalDate? = null) : IAlleyState
