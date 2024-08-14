package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import uk.co.thomasc.thealley.devices.state.IAlleyState

@Serializable
sealed interface IAlleyConfig<U : IAlleyState> : IAlleyConfigBase {
    val name: String

    @Transient
    val defaultState: U

    @Transient
    val stateSerializer: KSerializer<U>
}

// Helper so that the base class isn't generic
@Serializable
sealed interface IAlleyConfigBase

interface IAlleyRelayConfig

interface IAlleyLightConfig : IAlleyRelayConfig

interface IAlleyDualRelayConfig : IAlleyRelayConfig

interface IAlleyDualLightConfig : IAlleyDualRelayConfig, IAlleyLightConfig
