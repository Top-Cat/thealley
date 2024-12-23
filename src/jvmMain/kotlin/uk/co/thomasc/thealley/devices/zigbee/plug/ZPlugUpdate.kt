package uk.co.thomasc.thealley.devices.zigbee.plug

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeOTAInfo
import uk.co.thomasc.thealley.devices.zigbee.ZigbeePowerMonitoring
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateMains
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateOTA
import uk.co.thomasc.thealley.devices.zigbee.relay.ZRelayAction
import uk.co.thomasc.thealley.devices.zigbee.relay.ZigbeeUpdateRelay

@Serializable
data class ZPlugUpdate(
    override val linkquality: Int,

    override val current: Float,
    override val energy: Float,
    override val power: Float,
    override val voltage: Float,

    @SerialName("power_outage_memory")
    val powerOutageMemory: ZigbeePOM,
    override val state: ZRelayAction,
    @SerialName("child_lock")
    val childLock: ZPlugChildLock,
    @SerialName("indicator_mode")
    val indicatorMode: ZPlugIndicator,

    @SerialName("update_available")
    override val updateAvailable: Boolean? = null,
    override val update: ZigbeeOTAInfo? = null
) : ZigbeeUpdateMains, ZigbeePowerMonitoring, ZigbeeUpdateRelay, ZigbeeUpdateOTA
