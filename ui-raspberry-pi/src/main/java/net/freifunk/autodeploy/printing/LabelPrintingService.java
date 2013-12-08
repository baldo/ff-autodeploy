package net.freifunk.autodeploy.printing;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.firmware.FirmwareConfiguration;

/**
 * Service for printing labels with configuration and device details.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface LabelPrintingService {

    /**
     * Print a label containing the given details.
     */
    void printLabel(DetailedDevice device, FirmwareConfiguration configuration);
}
