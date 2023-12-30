package uk.co.thomasc.thealley.devices.energy.tado

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TadoState(val lastMeterReadDate: LocalDate? = null)
