package net.freifunk.autodeploy.firmware;

import java.io.File;
import java.util.Set;

import net.freifunk.autodeploy.device.Device;

import com.google.common.collect.Multimap;

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

    /**
     * Looks in the given directory for firmware images. A {@link Multimap} of {@link Device}s and
     * their available {@link Firmware}s will be returned.
     */
    Multimap<Device, Firmware> getAvailableDeviceFirmwareMappings(File firmwareImageDirectory);

    /**
     * Get the firmware file.
     */
    File findFirmwareImage(File firmwareImageDirectory, Device device, Firmware firmware);
}
