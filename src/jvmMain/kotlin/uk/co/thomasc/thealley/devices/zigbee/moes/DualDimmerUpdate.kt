package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdate
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction

@Serializable
data class DualDimmerUpdate(
    override val linkquality: Int,

    @SerialName("backlight_mode")
    val backlightMode: ZigbeeBacklightMode,

    @SerialName("state_l1")
    val state1: ZRelayAction,
    @SerialName("state_l2")
    val state2: ZRelayAction,
    @SerialName("brightness_l1")
    val brightness1: Int,
    @SerialName("brightness_l2")
    val brightness2: Int,

    @SerialName("countdown_l1")
    val countdown1: Int,
    @SerialName("countdown_l2")
    val countdown2: Int,

    @SerialName("max_brightness_l1")
    val maxBrightness1: Int,
    @SerialName("max_brightness_l2")
    val maxBrightness2: Int,
    @SerialName("min_brightness_l1")
    val minBrightness1: Int,
    @SerialName("min_brightness_l2")
    val minBrightness2: Int,

    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior
) : ZigbeeUpdate
