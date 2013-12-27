package net.freifunk.autodeploy.firmware;

import java.net.URI;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.device.Device;

/**
 * Configures the Freifunk firmware on a freshly deployed device.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface FirmwareConfigurator {

    /**
     * Wheter the device needs to be rewired before configuration.
     */
    boolean requiresRewiring(Device device);

    /**
     * Configures the Freifunk firmware. The given password will be set for the root user on the device.
     */
    FirmwareConfiguration configure(String password, String nodename);

    /**
     * Whether the {@link FirmwareConfigurator} supports registration of nodes.
     */
    boolean supportsNodeRegistration();

    /**
     * Register a node via the given configuration.
     *
     * @return An update token or <code>null</code> in case of an error or no token being available.
     */
    String registerNode(FirmwareConfiguration configuration, DetailedDevice device);

    /**
     * @return The {@link URI} for updating a nodes data. May be null if not supported.
     */
    URI getNodeUpdateUri();
}
