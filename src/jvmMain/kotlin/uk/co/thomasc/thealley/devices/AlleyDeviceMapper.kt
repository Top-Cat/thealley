package uk.co.thomasc.thealley.devices

import kotlin.reflect.KClass

abstract class AlleyDeviceMapper {
    suspend inline fun <reified T : AlleyDevice<*, *, *>> getDevice(id: Int): T? = getDevice(id, T::class)
    abstract suspend fun <T : AlleyDevice<*, *, *>> getDevice(id: Int, clazz: KClass<T>): T?
    suspend inline fun <reified T : AlleyDevice<*, *, *>> getDevices(): List<T> = getDevices(T::class)
    abstract suspend fun <T : AlleyDevice<*, *, *>> getDevices(clazz: KClass<T>): List<T>
    abstract suspend fun register(device: AlleyDevice<*, *, *>)
    abstract suspend fun deregister(device: AlleyDevice<*, *, *>)
}
