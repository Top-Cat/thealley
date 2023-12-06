package uk.co.thomasc.thealley.devices

import kotlinx.serialization.json.JsonPrimitive

interface IAlleyStats {
    val props: MutableMap<String, JsonPrimitive>
}
