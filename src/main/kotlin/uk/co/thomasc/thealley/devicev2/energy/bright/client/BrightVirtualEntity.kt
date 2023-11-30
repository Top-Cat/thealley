package uk.co.thomasc.thealley.devicev2.energy.bright.client

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BrightVirtualEntity(
    val clone: Boolean,
    val active: Boolean,
    val applicationId: String,
    val veTypeId: String,
    val postalCode: String,
    val resources: List<BrightVEResource>,
    val ownerId: String,
    val name: String,
    val veChildren: List<JsonElement>,
    val veId: String,
    val updatedAt: Instant,
    val createdAt: Instant
)
