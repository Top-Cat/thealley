package uk.co.thomasc.thealley.devices.energy.bright.client

import kotlinx.serialization.Serializable

@Serializable
data class BrightVEResource(
    override val resourceId: String,
    override val resourceTypeId: String,
    override val name: String
) : BrightResource()
