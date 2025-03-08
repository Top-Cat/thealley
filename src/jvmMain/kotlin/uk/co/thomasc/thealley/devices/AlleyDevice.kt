package uk.co.thomasc.thealley.devices

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.co.thomasc.thealley.devices.state.IAlleyState
import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.GoogleHomeTrait
import uk.co.thomasc.thealley.web.google.AlleyDeviceInfo
import java.io.Closeable

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig<U>, U : IAlleyState>(val id: Int, val config: T, state: U, private val stateStore: IStateUpdater<U>) : Closeable {
    private var currentState = state
    protected val state
        get() = currentState

    private lateinit var shim: AlleyEventBusShim
    suspend fun create(bus: AlleyEventBus) {
        shim = AlleyEventBusShim(bus)
        init(shim)
    }

    open suspend fun init(bus: AlleyEventBusShim) { }
    suspend fun updateState(state: U) =
        if (this.currentState != state) {
            stateStore.saveState(state)
            this.currentState = state
            true
        } else {
            false
        }

    private val mutex = Mutex()
    suspend fun updateState(block: suspend (U) -> U) =
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

    suspend fun getStateAsString() = stateStore.encoded(state)

    override fun close() {
        // Do nothing by default
    }

    suspend fun finalise() {
        shim.close()
        close()
    }
}
