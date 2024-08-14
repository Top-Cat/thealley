package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName

enum class TexecomZoneType {
    @SerialName("Not used")
    NotUsed,

    @SerialName("Entry/Exit 1")
    EntryExit1,

    @SerialName("Entry/Exit 2")
    EntryExit2,

    @SerialName("Guard")
    Guard,

    @SerialName("Guard Access")
    GuardAccess,

    @SerialName("24Hr Audible")
    TwentyFourHourAudible,

    @SerialName("24Hr Silent")
    TwentyFourHourSilent,

    @SerialName("PA Audible")
    PAAudible,

    @SerialName("PA Silent")
    PASilent,

    @SerialName("Fire")
    Fire,

    @SerialName("Medical")
    Medical,

    @SerialName("24Hr Gas")
    TwentyFourHourGas,

    @SerialName("Auxiliary")
    Auxiliary,

    @SerialName("Tamper")
    Tamper,

    @SerialName("Exit Terminator")
    ExitTerminator,

    @SerialName("Moment Key")
    MomentKey,

    @SerialName("Latch Key")
    LatchKey,

    @SerialName("Security")
    Security,

    @SerialName("Omit Key")
    OmitKey,

    @SerialName("Custom")
    Custom,

    @SerialName("Conf PA audible")
    ConfirmedPAAudible,

    @SerialName("Conf PA silent")
    ConfirmedPASilent
}
