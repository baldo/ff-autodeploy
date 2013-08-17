package net.freifunk.autodeploy.firmware;

/**
 * Configures the Freifunk firmware on a freshly deployed device.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface FirmwareConfigurator {

    /**
     * Configures the Freifunk firmware. The given password will be set for the root user on the device.
     */
    void configure(String password, String nodename);
}
