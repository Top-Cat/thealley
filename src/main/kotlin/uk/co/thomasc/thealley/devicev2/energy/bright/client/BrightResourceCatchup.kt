package uk.co.thomasc.thealley.devicev2.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightResourceCatchup(
    val status: String,
    override val resourceId: String,
    override val resourceTypeId: String,
    override val name: String,
    val classifier: String,
    val data: BrightCatchupResult
) : BrightResource()
