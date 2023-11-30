package uk.co.thomasc.thealley.devicev2.energy.bright.client

abstract class BrightResource {
    abstract val resourceId: String
    abstract val resourceTypeId: String
    abstract val name: String

    val type by lazy { BrightResourceType.fromUUID(resourceTypeId) }
}
