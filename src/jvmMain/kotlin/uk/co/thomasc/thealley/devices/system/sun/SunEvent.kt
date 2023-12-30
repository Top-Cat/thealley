package uk.co.thomasc.thealley.devices.system.sun

import uk.co.thomasc.thealley.devices.IAlleyEvent

interface SunEvent : IAlleyEvent

object SunRiseEvent : SunEvent
object SunSetEvent : SunEvent
