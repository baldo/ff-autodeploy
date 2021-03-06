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
package net.freifunk.autodeploy.ui.commandline;

import static net.freifunk.autodeploy.AutoDeployOptions.Command.RUN_PHASES;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_FIRMWARE_LIST;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_HELP;
import static net.freifunk.autodeploy.AutoDeployOptions.Command.SHOW_MODEL_LIST;
import static net.freifunk.autodeploy.Phase.CONFIGURE;
import static net.freifunk.autodeploy.Phase.DEPLOY;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import net.freifunk.autodeploy.AutoDeployOptions;
import net.freifunk.autodeploy.PhaseOptions;
import net.freifunk.autodeploy.PhaseOptions.ConfigurePhaseOptions;
import net.freifunk.autodeploy.PhaseOptions.DeployPhaseOptions;
import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceDeployer;
import net.freifunk.autodeploy.device.DeviceService;
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareConfigurator;
import net.freifunk.autodeploy.firmware.FirmwareService;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Main class for running the auto deployer via command line.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class CommandLineMain {

    private static final Logger LOG = LoggerFactory.getLogger(CommandLineMain.class);

    @Inject private CommandLineParser _commandLineParser;
    @Inject private FirmwareService _firmwareService;
    @Inject private DeviceService _deviceService;
    @Inject private WebDriver _webDriver;

    public CommandLineMain() {
        Guice.createInjector(new CommandLineUIModule()).injectMembers(this);
    }

    /**
    * Main entry point for the auto deployer.
    */
    public static void main(final String[] args) {
        new CommandLineMain().run(args);
    }

    private void run(final String[] args) {
        try {
            final AutoDeployOptions options = _commandLineParser.parse(args);

            if (options.shallPerform(SHOW_HELP)) {
                LOG.info(_commandLineParser.getHelpText());
            }

            if (options.shallPerform(SHOW_FIRMWARE_LIST)) {
                LOG.info(formatFirmwareList());
            }

            if (options.shallPerform(SHOW_MODEL_LIST)) {
                LOG.info(formatModelList());
            }

            if (options.shallPerform(RUN_PHASES)) {
                if (!options.arePhasesSpecified()) {
                    throw new IllegalArgumentException("No actions specified.");
                }

                if (options.hasPhase(DEPLOY)) {
                    final PhaseOptions phaseOptions = options.getPhaseOptions(DEPLOY);
                    if (!(phaseOptions instanceof DeployPhaseOptions)) {
                        throw new IllegalStateException("Options of deploy phase have wrong class: " + phaseOptions.getClass());
                    }
                    final DeployPhaseOptions deployOptions = (DeployPhaseOptions) phaseOptions;

                    final Device device;
                    if (deployOptions.shallAutodetectDevice()) {
                        device = _deviceService.autodetectDevice();
                        Preconditions.checkState(device != null, "Could not detect any device.");
                    } else {
                        device = deployOptions.getDevice();
                    }
                    Preconditions.checkState(device != null, "Device should not be null.");
                    final DeviceDeployer deployer = _deviceService.getDeployer(device);
                    deployer.deploy(deployOptions.getFirmwareImage());
                }

                if (options.hasPhase(CONFIGURE)) {
                    final PhaseOptions phaseOptions = options.getPhaseOptions(CONFIGURE);
                    if (!(phaseOptions instanceof ConfigurePhaseOptions)) {
                        throw new IllegalStateException("Options of configure phase have wrong class: " + phaseOptions.getClass());
                    }
                    final ConfigurePhaseOptions configureOptions = (ConfigurePhaseOptions) phaseOptions;
                    final FirmwareConfigurator configurator = _firmwareService.getConfigurator(configureOptions.getFirmware());

                    final String password;
                    if (configureOptions.shallGeneratePassword()) {
                        password = generatePassword();
                    } else {
                        password = configureOptions.getPassword();
                    }

                    configurator.configure(
                        password,
                        configureOptions.getNodeName()
                    );
                }
            }
        } catch (final CommandLineParsingException | FileNotFoundException e) {
            LOG.error(e.getMessage());
            System.exit(1);
        } catch (final Throwable e) {
            LOG.error("An unexpected error occured.", e);
            System.exit(255);
        } finally {
            // tear down selenium
            _webDriver.close();
        }
    }

    private static String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    private String formatFirmwareList() {
        final Set<Firmware> supportedFirmwares = _firmwareService.getSupportedFirmwares();
        final List<Firmware> sortedFirmwares = Ordering.<Firmware>natural().sortedCopy(supportedFirmwares);

        final StringBuilder output = new StringBuilder();
        output.append("Supported firmwares:");
        for (final Firmware firmware: sortedFirmwares) {
            output.append("\n    ");
            output.append(firmware.getName());
            output.append(" - ");
            output.append(firmware.getDisplayName());
            output.append(" (");
            output.append(firmware.getFirmwareUri());
            output.append(")");
        }
        return output.toString();
    }

    private String formatModelList() {
        final Set<Device> supportedDevices = _deviceService.getSupportedDevices();
        final List<Device> sortedDevices = Ordering.<Device>natural().sortedCopy(supportedDevices);

        final StringBuilder output = new StringBuilder();
        output.append("Supported models:");
        for (final Device device: sortedDevices) {
            output.append("\n    ");
            output.append(device.asString());
        }
        return output.toString();
    }
}
