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
package net.freifunk.autodeploy.ui.pi;

import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.BUTTON;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.LEFT;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.RIGHT;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import net.freifunk.autodeploy.device.DetailedDevice;
import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceDeployer;
import net.freifunk.autodeploy.device.DeviceService;
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareConfiguration;
import net.freifunk.autodeploy.firmware.FirmwareConfigurator;
import net.freifunk.autodeploy.firmware.FirmwareService;
import net.freifunk.autodeploy.printing.LabelPrintingService;
import net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver;
import net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent;
import net.freifunk.autodeploy.ui.pi.peripherals.LCDDriver;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.browserlaunchers.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Main class for running the auto deployer on the Raspberry Pi.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class RaspberryPiMain {

    private static final Logger LOG = LoggerFactory.getLogger(RaspberryPiMain.class);

    @Inject private JoystickDriver _joystickDriver;
    @Inject private LCDDriver _lcdDriver;
    @Inject private DeviceService _deviceService;
    @Inject private FirmwareService _firmwareService;
    @Inject private LabelPrintingService _labelPrintingService;

    public RaspberryPiMain() {
        Guice.createInjector(new RaspberryPiUIModule()).injectMembers(this);
    }

    public static void main(final String args[]) throws InterruptedException {
        LOG.debug("Running main.");

        new RaspberryPiMain().run(args);
    }

    private void run(final String[] args) {
        LOG.debug("Application context bound.");

        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Invalid commandline arguments. Expected firmware image path only.");
        }

        final File firmwareImageDirectory = new File(args[0]);
        LOG.debug("Using firmware from: " + firmwareImageDirectory);

        try {
            _joystickDriver.init();
            _lcdDriver.init();

            LOG.debug("Initialization done.");

            while (true) {
                final Device device = detectDevice();

                LOG.debug("Checking available firmware images.");

                final Multimap<Device, Firmware> availableFirmwares = _firmwareService.getAvailableDeviceFirmwareMappings(firmwareImageDirectory);
                final Collection<Firmware> deviceFirmwares = availableFirmwares.get(device);

                if (deviceFirmwares == null || deviceFirmwares.isEmpty()) {
                    LOG.debug("No matching image found. Waiting for confirmation.");

                    _lcdDriver.writeLines("No firmware", "found. :-(  [OK]");
                    waitForButton();
                } else {
                    final Firmware firmware = chooseFirmware(deviceFirmwares);

                    if (firmware == null) {
                        LOG.debug("No firmware selected. Aborting.");
                        continue;
                    }

                    LOG.debug("Firmware selected: " + firmware.getName());

                    final String password = generateRandomPassword();
                    final String nodename = generateRandomNodename(firmware);

                    LOG.debug("Password and nodename generated.");

                    final DeviceDeployer deployer = _deviceService.getDeployer(device);
                    final FirmwareConfigurator configurator = _firmwareService.getConfigurator(firmware);

                    LOG.debug("Deployer and configurator loaded.");

                    final File firmwareImage = _firmwareService.findFirmwareImage(firmwareImageDirectory, device, firmware);

                    if (firmwareImage == null) {
                        LOG.debug("No firmware image found. Waiting for confirmation.");

                        _lcdDriver.writeLines("No firmware", "found. :-(  [OK]");
                        waitForButton();
                        continue;
                    }

                    LOG.debug("Matching firmware image: " + firmwareImage);

                    final DetailedDevice detailedDevice;
                    try {
                        LOG.debug("Starting installation.");

                        _lcdDriver.writeLines("Installing...", "Please wait...");
                        detailedDevice = deployer.deploy(firmwareImage);

                        LOG.debug("Installation done.");
                    } catch (final FileNotFoundException e) {
                        throw new IllegalStateException("Could not deploy firmware.", e);
                    }

                    if (configurator.requiresRewiring(device)) {
                        LOG.debug("Rewiring is required for device. Waiting for confirmation.");
                        _lcdDriver.writeLines("Connect WAN port", "            [OK]");
                        waitForButton();
                    }

                    LOG.debug("Starting configuration.");

                    _lcdDriver.writeLines("Configuring...", "Please wait...");
                    final FirmwareConfiguration configuration = configurator.configure(password, nodename);

                    LOG.debug("Configuration done.");

                    final String updateToken;
                    final URI updateUri;
                    if (configurator.supportsNodeRegistration()) {
                        LOG.debug("Starting node registration.");

                        updateToken = configurator.registerNode(configuration, detailedDevice);

                        LOG.debug("Node registration done.");

                        updateUri = configurator.getNodeUpdateUri();
                    } else {
                        LOG.debug("Node registration not supported. Skipping.");

                        updateToken = null;
                        updateUri = null;
                    }

                    LOG.debug("Printing label.");

                    _labelPrintingService.printLabel(
                        firmware,
                        detailedDevice,
                        configuration,
                        updateToken,
                        updateUri
                    );

                    LOG.debug("We are done. Waiting for confirmation.");

                    _lcdDriver.writeLines("We are done...", "            [OK]");
                    waitForButton();
                }
            }
        }
        finally {
            LOG.debug("Shutting down.");

            try {
                _joystickDriver.shutdown();
            }
            finally {
                _lcdDriver.shutdown();
            }
        }
    }

    private Device detectDevice() {
        LOG.debug("Waiting for confirmation before detecting device.");

        _lcdDriver.writeLines("Connect device", "            [OK]");
        waitForButton();

        Device device = null;
        while (device == null) {
            LOG.debug("Starting detection.");

            _lcdDriver.writeLines("Detecting...", "Please wait...");
            device = _deviceService.autodetectDevice();

            if (device == null) {
                LOG.debug("No device found. Waiting for confirmation.");

                _lcdDriver.writeLines("Detection failed", "Retry?     [Yes]");
                waitForButton();
            }
        }

        LOG.debug("Device detected: " + device.asString());

        _lcdDriver.writeLines(device.asString(), "          [Next]");
        waitForButton();

        return device;
    }

    private Firmware chooseFirmware(final Collection<Firmware> deviceFirmwares) {
        LOG.debug("Selecting firmware.");

        final List<Firmware> firmwares = Ordering.natural().sortedCopy(deviceFirmwares);
        final int numFirmwares = firmwares.size();
        Preconditions.checkState(numFirmwares > 0, "No firmwares found.");

        final int numOptions = numFirmwares + 1;

        int index = 0;
        JoystickEvent event = null;

        while (event != BUTTON) {
            Sleeper.sleepTight(100);

            _lcdDriver.writeLines("<-  Firmware  ->", getSelection(firmwares, index));

            _joystickDriver.flush();
            event = _joystickDriver.read();

            if (event == LEFT) {
                index = (index - 1 + numOptions) % numOptions;
            } else if (event == RIGHT) {
                index = (index + 1) % numOptions;
            }
        }

        return index == numFirmwares ? null : firmwares.get(index);
    }

    private String getSelection(final List<Firmware> firmwares, final int index) {
        if (index == firmwares.size()) {
            return "Cancel      [OK]";
        } else {
            return firmwares.get(index).getName();
        }
    }

    private void waitForButton() {
        while (_joystickDriver.read() != BUTTON) {
        }
    }

    private String generateRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    private String generateRandomNodename(final Firmware firmware) {
        return firmware.getName() + "_" + RandomStringUtils.randomAlphanumeric(8);
    }
}
