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
