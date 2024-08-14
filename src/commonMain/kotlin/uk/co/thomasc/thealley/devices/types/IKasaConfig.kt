package uk.co.thomasc.thealley.devices.types

import uk.co.thomasc.thealley.devices.state.kasa.IKasaState

interface IKasaConfig<U : IKasaState> : IAlleyConfig<U> {
    val host: String
}
