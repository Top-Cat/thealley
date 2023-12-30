package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.long

@Serializable
data class BrightResourceReadings(
    val status: String,
    override val name: String,
    override val resourceTypeId: String,
    override val resourceId: String,
    val query: BrightResourceQuery,
    val data: List<List<JsonPrimitive>>,
    val units: String,
    val classifier: String
) : BrightResource() {
    val readings by lazy {
        data.map { BrightReading(Instant.fromEpochSeconds(it[0].long), it[1].floatOrNull) }
    }
}
