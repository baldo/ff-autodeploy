/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
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
