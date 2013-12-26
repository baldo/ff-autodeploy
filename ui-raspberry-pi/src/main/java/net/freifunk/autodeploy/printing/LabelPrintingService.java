package net.freifunk.autodeploy.printing;

import java.net.URI;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.firmware.Firmware;
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
    void printLabel(
        Firmware firmware,
        DetailedDevice device,
        FirmwareConfiguration configuration,
        String updateToken,
        URI updateUri
    );
}
