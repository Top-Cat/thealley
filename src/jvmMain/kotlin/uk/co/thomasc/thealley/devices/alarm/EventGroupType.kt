package uk.co.thomasc.thealley.devices.alarm

import EnumAsIntSerializer
import kotlinx.serialization.Serializable

@Serializable(with = EventGroupType.EventGroupTypeSerializer::class)
enum class EventGroupType(val typeId: Int) {
    NotReported(0),
    PriorityAlarm(1),
    PriorityAlarmRestore(2),
    Alarm(3),
    Restore(4),
    Open(5),
    Close(6),
    Bypassed(7),
    Unbypassed(8),
    MaintenanceAlarm(9),
    MaintenanceRestore(10),
    TamperAlarm(11),
    TamperRestore(12),
    TestStart(13),
    TestEnd(14),
    Disarmed(15),
    Armed(16),
    Tested(17),
    Started(18),
    Ended(19),
    Fault(20),
    Omitted(21),
    Reinstated(22),
    Stopped(23),
    Start(24),
    Deleted(25),
    Active(26),
    NotUsed(27),
    Changed(28),
    LowBattery(29),
    Radio(30),
    Deactivated(31),
    Added(32),
    BadAction(33),
    PATimerReset(34),
    PAZoneLockout(35),
    FireAlarm(129),
    FireAlarmEnd(130);

    class EventGroupTypeSerializer : EnumAsIntSerializer<EventGroupType>(
        "EventGroupType",
        { it.typeId },
        { v -> EventGroupType.entries.first { it.typeId == v } }
    )
}
