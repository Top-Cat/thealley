package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.web.google.AlleyDeviceInfo
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

    private val mutex = Mutex()
    suspend fun updateState(block: (U) -> U) =
        mutex.withLock {
            updateState(block(state))
        }

    private var googleHome: GoogleHomeInfo? = null

    val gh
        get() = googleHome

    fun registerGoogleHomeDevice(type: DeviceType, willReportState: Boolean, vararg traits: GoogleHomeTrait<*>) =
        registerGoogleHomeDevice(type, willReportState, null, *traits)

    fun registerGoogleHomeDevice(type: DeviceType, willReportState: Boolean, deviceInfo: (() -> AlleyDeviceInfo)? = null, vararg traits: GoogleHomeTrait<*>) {
        type.requiredTraits.firstOrNull { required -> !traits.any { trait -> required.isInstance(trait) } }?.let {
            throw MissingTraitException(it)
        }

        googleHome = GoogleHomeInfo(type, traits.toSet(), willReportState, deviceInfo)
    }

    override fun close() {
        // Do nothing by default
    }
}

data class GoogleHomeInfo(
    val type: DeviceType,
    val traits: Set<GoogleHomeTrait<*>>,
    val willReportState: Boolean = false,
    val deviceInfo: (() -> AlleyDeviceInfo)? = null
)

class MissingTraitException(kClass: KClass<out GoogleHomeTrait<out IGoogleHomeCommand<*>>>) : Exception("Missing required trait: ${kClass.simpleName}")
data class GetStateException(val errorCode: GoogleHomeErrorCode) : Exception("Error getting device state $errorCode")
