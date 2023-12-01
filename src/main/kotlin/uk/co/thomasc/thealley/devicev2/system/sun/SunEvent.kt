package uk.co.thomasc.thealley.devicev2.system.sun

import uk.co.thomasc.thealley.devicev2.IAlleyEvent

interface SunEvent : IAlleyEvent

object SunRiseEvent : SunEvent
object SunSetEvent : SunEvent
