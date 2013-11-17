package net.freifunk.autodeploy.device;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Deploys the Freifunk firmware to a device.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface DeviceDeployer {

    /**
     * Tries to detect the device connected.
     *
     * @return <code>null</code> iff the device could not be detected or is
     *         not supported by the {@link DeviceDeployer}.
     */
    Device autodetect();

    /**
     * Deploys the specified firmware image.
     */
    void deploy(File firmwareImage) throws FileNotFoundException;
}
