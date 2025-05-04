package uk.co.thomasc.thealley.devices.alarm

import uk.co.thomasc.thealley.EnumAsIntSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ZoneState.ZoneStateSerializer::class)
enum class ZoneState(val enc: Int) {
    Secure(0),
    Active(1),
    Tampered(2),
    Short(3);

    class ZoneStateSerializer : EnumAsIntSerializer<ZoneState>(
        "ZoneState",
        { it.enc },
        { v -> ZoneState.entries.first { it.enc == v } }
    )
}
