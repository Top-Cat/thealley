package uk.co.thomasc.thealley.devices.types

import kotlinx.serialization.Serializable

@Serializable
sealed interface IAlleyConfig {
    val name: String
}

interface IAlleyRelayConfig

interface IAlleyLightConfig : IAlleyRelayConfig

interface IAlleyDualRelayConfig : IAlleyRelayConfig

interface IAlleyDualLightConfig : IAlleyDualRelayConfig, IAlleyLightConfig