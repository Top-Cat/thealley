package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.devices.types.IAlleyConfig
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.safeCast
import kotlin.time.Duration

abstract class IConfigField<T : IAlleyConfig<*>, U : Any> {
    abstract val name: String
    abstract val setter: (T, U) -> T
    abstract val getter: (T) -> U
    abstract val clazz: KClass<T>

    fun set(config: IAlleyConfig<*>, v: U) =
        clazz.safeCast(config)?.let { setter(it, v) } ?: config

    fun get(config: IAlleyConfig<*>): U? = clazz.safeCast(config)?.let { getter(it) }
}

data class StringConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> String, override val setter: (T, String) -> T, override val clazz: KClass<T>) : IConfigField<T, String>()
data class StringListConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> List<String>, override val setter: (T, List<String>) -> T, override val clazz: KClass<T>) : IConfigField<T, List<String>>()
data class PasswordConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> String, override val setter: (T, String) -> T, override val clazz: KClass<T>) : IConfigField<T, String>()
data class DurationConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> Duration, override val setter: (T, Duration) -> T, override val clazz: KClass<T>) : IConfigField<T, Duration>()
data class DoubleConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> Double, override val setter: (T, Double) -> T, override val clazz: KClass<T>) : IConfigField<T, Double>()
data class IntConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> Int, override val setter: (T, Int) -> T, override val clazz: KClass<T>) : IConfigField<T, Int>()
data class BooleanConfigField<T : IAlleyConfig<*>>(override val name: String, override val getter: (T) -> Boolean, override val setter: (T, Boolean) -> T, override val clazz: KClass<T>) : IConfigField<T, Boolean>()
data class DeviceConfigField<T : IAlleyConfig<*>>(override val name: String, val filter: (IAlleyConfig<*>) -> Boolean, override val getter: (T) -> Int, override val setter: (T, Int) -> T, override val clazz: KClass<T>) : IConfigField<T, Int>()
data class DeviceListConfigField<T : IAlleyConfig<*>>(override val name: String, val filter: (IAlleyConfig<*>) -> Boolean, override val getter: (T) -> List<Int>, override val setter: (T, List<Int>) -> T, override val clazz: KClass<T>) : IConfigField<T, List<Int>>()

@JvmName("fieldEditorString")
inline fun <reified T : IAlleyConfig<*>> KProperty1<T, String>.fieldEditor(label: String? = null, password: Boolean = false, noinline setter: (T, String) -> T) =
    if (password) {
        PasswordConfigField(label ?: name, ::get, setter, T::class)
    } else {
        StringConfigField(label ?: name, ::get, setter, T::class)
    }

inline fun <reified T : IAlleyConfig<*>> KProperty1<T, List<String>>.fieldEditor(label: String? = null, noinline setter: (T, List<String>) -> T) =
    StringListConfigField(label ?: name, ::get, setter, T::class)

inline fun <reified T : IAlleyConfig<*>> KProperty1<T, Duration>.fieldEditor(label: String? = null, noinline setter: (T, Duration) -> T) =
    DurationConfigField(label ?: name, ::get, setter, T::class)

inline fun <reified T : IAlleyConfig<*>> KProperty1<T, Double>.fieldEditor(label: String? = null, noinline setter: (T, Double) -> T) =
    DoubleConfigField(label ?: name, ::get, setter, T::class)

inline fun <reified T : IAlleyConfig<*>> KProperty1<T, Boolean>.fieldEditor(label: String? = null, noinline setter: (T, Boolean) -> T) =
    BooleanConfigField(label ?: name, ::get, setter, T::class)

@JvmName("fieldEditorInt")
inline fun <reified T : IAlleyConfig<*>> KProperty1<T, Int>.fieldEditor(label: String? = null, noinline filter: ((IAlleyConfig<*>) -> Boolean)? = null, noinline setter: (T, Int) -> T) =
    if (filter != null) {
        DeviceConfigField(label ?: name, filter, ::get, setter, T::class)
    } else {
        IntConfigField(label ?: name, ::get, setter, T::class)
    }

inline fun <reified T : IAlleyConfig<*>> KProperty1<T, List<Int>>.fieldEditor(label: String? = null, noinline filter: (IAlleyConfig<*>) -> Boolean = { true }, noinline setter: (T, List<Int>) -> T) =
    DeviceListConfigField(label ?: name, filter, ::get, setter, T::class)

interface IConfigEditable<T : IAlleyConfig<*>> {
    val fields: List<IConfigField<T, *>>
}

class SimpleConfigEditable<T : IAlleyConfig<*>>(override val fields: List<IConfigField<T, *>>) : IConfigEditable<T>
