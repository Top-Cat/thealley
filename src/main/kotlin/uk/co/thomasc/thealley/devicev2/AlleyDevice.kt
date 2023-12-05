package uk.co.thomasc.thealley.devicev2

import uk.co.thomasc.thealley.devicev2.types.IAlleyConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.command.IGoogleHomeCommand
import uk.co.thomasc.thealley.google.trait.IGoogleHomeTrait
import java.io.Closeable
import kotlin.reflect.KClass

abstract class AlleyDevice<A : AlleyDevice<A, T, U>, T : IAlleyConfig, U : Any>(val id: Int, val config: T, state: U, private val stateStore: IStateUpdater<U>) : Closeable {
    private var currentState = state
    protected val state
        get() = currentState

    open suspend fun init(bus: AlleyEventBus) { }
    suspend fun updateState(state: U) {
        if (this.currentState != state) {
            stateStore.saveState(state)
            this.currentState = state
        }
    }

    private var googleHomeType: DeviceType? = null
    private var googleHomeTraits: Set<IGoogleHomeTrait<*>>? = null

    val ghType
        get() = googleHomeType
    val ghTraits
        get() = googleHomeTraits

    fun registerGoogleHomeDevice(type: DeviceType, vararg traits: IGoogleHomeTrait<*>) {
        type.requiredTraits.firstOrNull { required -> !traits.any { trait -> required.isInstance(trait) } }?.let {
            throw MissingTraitException(it)
        }

        googleHomeType = type
        googleHomeTraits = traits.toSet()
    }

    override fun close() {
        // Do nothing by default
    }
}

class MissingTraitException(kClass: KClass<out IGoogleHomeTrait<out IGoogleHomeCommand<*>>>) : Exception("Missing required trait: ${kClass.simpleName}")
