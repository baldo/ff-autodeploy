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
package net.freifunk.autodeploy;

import static net.freifunk.autodeploy.Phase.CONFIGURE;
import static net.freifunk.autodeploy.Phase.DEPLOY;

import java.io.File;

import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.firmware.Firmware;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * Options for a specific phase in the deployment process.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public abstract class PhaseOptions {

    /**
     * Options for the {@link Phase#DEPLOY} phase.
     *
     * @author Andreas Baldeau <andreas@baldeau.net>
     */
    public static final class DeployPhaseOptions extends PhaseOptions {

        private final Device _device;
        private final File _firmwareImage;
        private final boolean _autodetectDevice;

        private DeployPhaseOptions(
            final Device device,
            final boolean autodetectDevice,
            final File firmwareImage
        ) {
            super(DEPLOY);
            _device = device;
            _autodetectDevice = autodetectDevice;
            _firmwareImage = firmwareImage;
        }

        public Device getDevice() {
            return _device;
        }

        public boolean shallAutodetectDevice() {
            return _autodetectDevice;
        }

        public File getFirmwareImage() {
            return _firmwareImage;
        }
    }

    /**
     * Options for the {@link Phase#CONFIGURE} phase.
     *
     * @author Andreas Baldeau <andreas@baldeau.net>
     */
    public static final class ConfigurePhaseOptions extends PhaseOptions {

        private final Firmware _firmware;
        private final String _nodeName;
        private final String _password;

        private ConfigurePhaseOptions(
            final Firmware firmware,
            final String nodeName
        ) {
            this(firmware, nodeName, null);
        }

        private ConfigurePhaseOptions(
            final Firmware firmware,
            final String nodeName,
            final String password
        ) {
            super(CONFIGURE);
            _firmware = firmware;
            _nodeName = nodeName;
            _password = password;
        }

        public Firmware getFirmware() {
            return _firmware;
        }

        public String getNodeName() {
            return _nodeName;
        }

        public boolean shallGeneratePassword() {
            return _password == null;
        }

        public String getPassword() {
            Preconditions.checkState(
                _password != null,
                "Trying to get password, but it shall be generated."
            );

            return _password;
        }
    }

    public static final Function<PhaseOptions, Phase> GET_PHASE = new Function<PhaseOptions, Phase>() {

        @Override
        public Phase apply(final PhaseOptions options) {
            return options == null ? null : options.getPhase();
        }
    };

    private final Phase _phase;

    private PhaseOptions(final Phase phase) {
        _phase = phase;
    }

    /**
     * Options for deploying the firmware to the device.
     * @param autodetectDevice
     */
    public static PhaseOptions forDeployPhase(
        final Device device,
        final boolean autodetectDevice,
        final File firmwareImage
    ) {
        Preconditions.checkArgument((device == null) == autodetectDevice, "Either autodetection must be set or a device be given.");
        return new DeployPhaseOptions(device, autodetectDevice, firmwareImage);
    }

    /**
     * Options for configuring the firmware with a generated password.
     */
    public static PhaseOptions forConfigurePhase(
        final Firmware firmware,
        final String nodeName
    ) {
        return new ConfigurePhaseOptions(firmware, nodeName);
    }

    /**
     * Options for configuring the firmware with the given password.
     */
    public static PhaseOptions forConfigurePhase(
        final Firmware firmware,
        final String nodeName,
        final String password
    ) {
        Preconditions.checkArgument(password != null, "Password may not be null.");
        return new ConfigurePhaseOptions(firmware, nodeName, password);
    }

    public Phase getPhase() {
        return _phase;
    }
}
