package uk.co.thomasc.thealley.devicev2.energy.bright.client

enum class BrightResourceType(val uuid: String, val utility: BrightUtilityType, val metric: BrightMetricType) {
    ELECTRIC_CONSUMPTION("e3a5db34-6e0c-4221-9653-8d33e27511ba", BrightUtilityType.ELETRIC, BrightMetricType.CONSUMPTION),
    ELECTRIC_COST("78859e39-611e-4e84-a402-1d4460abcb56", BrightUtilityType.ELETRIC, BrightMetricType.COST),
    GAS_CONSUMPTION("08ab415f-d851-423f-adf4-c2b1e0529e27", BrightUtilityType.GAS, BrightMetricType.CONSUMPTION),
    GAS_COST("a6b95f41-771d-4bd2-99f4-93ee43c38f5a", BrightUtilityType.GAS, BrightMetricType.COST);

    companion object {
        private val map = entries.associateBy { it.uuid }
        fun fromUUID(uuid: String) = map[uuid]
    }
}
