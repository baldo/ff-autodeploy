package net.freifunk.hamburg.autodeploy.devices;

import java.io.File;

/**
 * Deploys the Freifunk firmware to a device.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface DeviceDeployer {

    /**
     * Deploys the specified firmware image. The given password will be set for the root user on the device.
     */
    void deploy(File firmwareImage, String password, String nodename);
}
