package uk.co.thomasc.thealley.devices.unifi

import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBusShim
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.types.UnifiConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.trait.NetworkControlTrait

class UnifiDevice(id: Int, config: UnifiConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<UnifiDevice, UnifiConfig, EmptyState>(id, config, state, stateStore) {

    override suspend fun init(bus: AlleyEventBusShim) {
        registerGoogleHomeDevice(
            DeviceType.NETWORK,
            false,
            NetworkControlTrait(
                NetworkControlTrait.NetworkControlAttributes(
                    supportsGettingGuestNetworkPassword = true,
                    supportsNetworkDownloadSpeedTest = true,
                    supportsNetworkUploadSpeedTest = true
                ),
                getNetworkState = {
                    NetworkControlTrait.NetworkControlState(
                        networkEnabled = true,
                        networkSettings = NetworkControlTrait.NetworkSettings(config.mainNetwork),
                        guestNetworkEnabled = true,
                        guestNetworkSettings = NetworkControlTrait.NetworkSettings(config.guestNetwork),
                        numConnectedDevices = 10,
                        networkUsageMB = 123.4f,
                        networkUsageUnlimited = true,
                        networkSpeedTestInProgress = false
                    )
                },
                getGuestPassword = { config.guestPassword }
            )
        )
    }
}
