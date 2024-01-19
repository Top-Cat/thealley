package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.SerialName

enum class TexecomEventType {
    EntryExit1, EntryExit2, Guard, GuardAccess, TwentyFourHourAudible, TwentyFourHourSilent, PAAudible, PASilent,
    Fire, Medical, TwentyFourHourGas, Auxiliary, Tamper, ExitTerminator, MomentKey, LatchKey, Security, OmitKey,
    Custom, ConfirmedPAAudible, ConfirmedPASilent, KeypadMedical, KeypadFire, KeypadAudiblePA, KeypadSilentPA,
    DuressCodeAlarm, AlarmActive, BellActive, Rearm, VerifiedCrossZoneAlarm, UserCode, ExitStarted, ExitError,
    EntryStarted, PartArmSuite, ArmedWithLineFault, OpenClose, PartArmed, AutoOpenClose, AutoArmDeferred,
    OpenAfterAlarm, RemoteOpenClose, QuickArm, RecentClosing, ResetAfterAlarm, PowerOPFault, ACFail, LowBattery,
    SystemPowerUp, MainsOverVoltage, TelephoneLineFault, FailToCommunicate, DownloadStart, DownloadEnd,
    LogCapacityAlert, DateChanged, TimeChanged, InstallerProgrammingStart, InstallerProgrammingEnd, PanelBoxTamper,
    BellTamper, AuxiliaryTamper, ExpanderTamper, KeypadTamper, ExpanderTrouble, RemoteKeypadTrouble, FireZoneTamper,
    ZoneTamper, KeypadLockout, CodeTamperAlarm, SoakTestAlarm, ManualTestTransmission, AutomaticTestTransmission,
    UserWalkTestStartEnd, NVMDefaultsLoaded, FirstKnock, DoorAccess, PartArm1, PartArm2, PartArm3, AutoArmingStarted,
    ConfirmedAlarm, ProxTag, AccessCodeChangedDeleted, ArmFailed, LogCleared, CommunicationPort,
    TAGSystemExitBatteryOK, TAGSystemExitBatteryLow, TAGSystemEntryBatteryOK, TAGSystemEntryBatteryLow,
    MicrophoneActivated, AVClearedDown, MonitoredAlarm, ExpanderLowVoltage, SupervisionFault, PAFromRemoteFOB,
    RFDeviceLowBattery, SiteDataChanged, RadioJamming, TestCallPassed, TestCallFailed, ZoneFault, ZoneMasked,
    FaultsOverridden, PSUACFail, PSUBatteryFail, PSULowOutputFail, PSUTamper, DoorAccess2, CIEReset, RemoteCommand,
    UserAdded, UserDeleted, ConfirmedPA, UserAcknowledged, PowerUnitFailure, BatteryChargerFault, ConfirmedIntruder,
    GSMTamper, RadioConfigFailure, QuickPartArm1, QuickPartArm2, QuickPartArm3, RemotePartArm1, RemotePartArm2,
    RemotePartArm3,

    @SerialName("iDLoopShorted")
    IDLoopShorted
}
