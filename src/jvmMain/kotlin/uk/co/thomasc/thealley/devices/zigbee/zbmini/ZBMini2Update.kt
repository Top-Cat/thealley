package uk.co.thomasc.thealley.devices.zigbee.zbmini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAInfo
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateOTA
import uk.co.thomasc.thealley.devices.zigbee.moes.ZigbeePowerOnBehavior
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class ZBMini2Update(
    override val linkquality: Int,

    override val state: ZRelayAction,

    @SerialName("update_available")
    override val updateAvailable: Boolean?,
    override val update: ZigbeeOTAInfo?,

    @SerialName("delayed_power_on_state")
    val delayedPowerOnState: Boolean = false,
    @SerialName("delayed_power_on_time")
    val delayedPowerOnTime: Int = 0,
    @SerialName("detach_relay_mode")
    val detachRelayMode: Boolean = false,
    @SerialName("external_trigger_mode")
    val externalTriggerMode: ExternalTriggerMode = ExternalTriggerMode.EDGE,
    @SerialName("power_on_behavior")
    val powerOnBehavior: ZigbeePowerOnBehavior,
    @SerialName("turbo_mode")
    val turboMode: Boolean = false,
) : ZigbeeUpdateRelay, ZigbeeUpdateOTA

enum class ExternalTriggerMode {
    @SerialName("toggle")
    EDGE,

    @SerialName("pulse")
    PULSE,

    @SerialName("following(off)")
    FOLLOWING_OFF,

    @SerialName("following(on)")
    FOLLOWING_ON
}
