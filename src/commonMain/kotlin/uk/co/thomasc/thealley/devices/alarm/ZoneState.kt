package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.EnumAsIntSerializer

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
