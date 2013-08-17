package net.freifunk.autodeploy.firmware;

import java.util.Set;

/**
 * Service for handling {@link Firmware}s.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public interface FirmwareService {

    /**
     * @return the supported {@link Firmware} or <code>null</code> if the firmware is not supported.
     */
    Firmware findSupportedFirmware(String firmwareString);

    /**
     * @return the supported {@link Firmware}s.
     */
    Set<Firmware> getSupportedFirmwares();

    /**
     * @return the firmware configurator for the given firmware.
     */
    FirmwareConfigurator getConfigurator(Firmware firmware);
}
