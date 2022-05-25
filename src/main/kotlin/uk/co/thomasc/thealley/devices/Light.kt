package uk.co.thomasc.thealley.devices

interface Light<out T> {
    fun setPowerState(value: Boolean): T

    fun setComplexState(brightness: Int? = null, hue: Int? = null, saturation: Int? = null, temperature: Int? = null, transitionTime: Int? = 1000): T

    fun getPowerState(): Boolean

    fun togglePowerState(): T
}
