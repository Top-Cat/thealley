package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode
import java.io.Closeable
import kotlin.reflect.KClass

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig, U : Any>(val id: Int, val config: T, state: U, private val stateStore: IStateUpdater<U>) : Closeable {
    private var currentState = state
    protected val state
        get() = currentState

    open suspend fun init(bus: AlleyEventBus) { }
    suspend fun updateState(state: U) =
        if (this.currentState != state) {
            stateStore.saveState(state)
            this.currentState = state
            true
        } else {
            false
        }

    private var googleHome: GoogleHomeInfo? = null

    val gh
        get() = googleHome

    fun registerGoogleHomeDevice(type: DeviceType, willReportState: Boolean, vararg traits: GoogleHomeTrait<*>) {
        type.requiredTraits.firstOrNull { required -> !traits.any { trait -> required.isInstance(trait) } }?.let {
            throw MissingTraitException(it)
        }

        googleHome = GoogleHomeInfo(type, traits.toSet(), willReportState)
    }

    override fun close() {
        // Do nothing by default
    }
}

data class GoogleHomeInfo(val type: DeviceType, val traits: Set<GoogleHomeTrait<*>>, val willReportState: Boolean = false)

class MissingTraitException(kClass: KClass<out GoogleHomeTrait<out IGoogleHomeCommand<*>>>) : Exception("Missing required trait: ${kClass.simpleName}")
data class GetStateException(val errorCode: GoogleHomeErrorCode) : Exception("Error getting device state $errorCode")
