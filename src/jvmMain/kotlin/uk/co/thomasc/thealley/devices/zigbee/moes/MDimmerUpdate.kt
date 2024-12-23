package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAInfo
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdate
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateOTA
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateDimmer

@Serializable
data class MDimmerUpdate(
    override val linkquality: Int,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior? = ZigbeePowerOnBehavior.PREVIOUS,

    @SerialName("update_available")
    override val updateAvailable: Boolean? = null,
    override val update: ZigbeeOTAInfo? = null,

    @SerialName("backlight_mode")
    val backlightMode: ZigbeeBacklightMode,
    @SerialName("light_type")
    val lightType: ZigbeeDimmerLightType,
    val countdown: Int? = null,

    @SerialName("max_brightness")
    val maxBrightness: Int,
    @SerialName("min_brightness")
    val minBrightness: Int,
    override val brightness: Int,
    override val state: ZRelayAction
) : ZigbeeUpdate, ZigbeeUpdateOTA, ZigbeeUpdateDimmer
