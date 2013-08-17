package net.freifunk.autodeploy.device;

import java.util.Set;

/**
 * Service for handling {@link Device} information.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface DeviceService {

    /**
     * @return the supported {@link Device} or <code>null</code> if the device is not supported.
     */
    Device findSupportedDevice(String deviceString);

    /**
     * @return the {@link DeviceDeployer} for the given {@link Device}.
     */
    DeviceDeployer getDeployer(Device device);

    /**
     * @return the supported {@link Device}s.
     */
    Set<Device> getSupportedDevices();
}
