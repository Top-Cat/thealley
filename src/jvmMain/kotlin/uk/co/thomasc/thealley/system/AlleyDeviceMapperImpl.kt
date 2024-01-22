package uk.co.thomasc.thealley.system

import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyDeviceMapper
import kotlin.reflect.KClass

internal class AlleyDeviceMapperImpl : AlleyDeviceMapper() {
    private val deviceList = mutableListOf<AlleyDevice<*, *, *>>()

    override suspend fun <T : AlleyDevice<*, *, *>> getDevice(id: Int, clazz: KClass<T>) =
        getDevices(clazz).firstOrNull { it.id == id }

    override suspend fun <T : AlleyDevice<*, *, *>> getDevices(clazz: KClass<T>): List<T> =
        deviceList.filterIsInstance(clazz.java)

    override suspend fun register(device: AlleyDevice<*, *, *>) {
        deviceList.add(device)
    }

    override suspend fun deregister(device: AlleyDevice<*, *, *>) {
        deviceList.remove(device)
    }
}
