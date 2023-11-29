package uk.co.thomasc.thealley.devicev2

import kotlinx.serialization.json.JsonPrimitive

interface IAlleyStats {
    val props: MutableMap<String, JsonPrimitive>
}
