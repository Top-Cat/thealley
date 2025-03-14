package uk.co.thomasc.thealley.devices.system

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class TickEvent(val now: Instant = Clock.System.now()) : IAlleyEvent
