package net.freifunk.autodeploy;

/**
 * Phase during the auto deployment.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public enum Phase {

    /**
     * Deploy the firmware to the device.
     */
    DEPLOY,

    /**
     * Configure the deployed firmware on the device.
     */
    CONFIGURE,
    ;
}
