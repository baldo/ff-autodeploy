package net.freifunk.autodeploy.ui.pi;

import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.BUTTON;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.LEFT;
import static net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver.JoystickEvent.RIGHT;

import java.io.File;
import java.io.FileNotFoundException;
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

    @Inject private JoystickDriver _joystickDriver;
    @Inject private LCDDriver _lcdDriver;
    @Inject private DeviceService _deviceService;
    @Inject private FirmwareService _firmwareService;
    @Inject private LabelPrintingService _labelPrintingService;

    public RaspberryPiMain() {
        Guice.createInjector(new RaspberryPiUIModule()).injectMembers(this);
    }

    public static void main(final String args[]) throws InterruptedException {
        new RaspberryPiMain().run(args);
    }

    private void run(final String[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("Invalid commandline arguments. Expected firmware image path only.");
        }

        final File firmwareImageDirectory = new File(args[0]);

        try {
            _joystickDriver.init();
            _lcdDriver.init();

            while (true) {
                final Device device = detectDevice();

                final Multimap<Device, Firmware> availableFirmwares = _firmwareService.getAvailableDeviceFirmwareMappings(firmwareImageDirectory);
                final Collection<Firmware> deviceFirmwares = availableFirmwares.get(device);

                if (deviceFirmwares == null || deviceFirmwares.isEmpty()) {
                    _lcdDriver.writeLines("No firmware", "found. :-(  [OK]");
                    waitForButton();
                } else {
                    final Firmware firmware = chooseFirmware(deviceFirmwares);

                    if (firmware == null) {
                        continue;
                    }

                    final String password = generateRandomPassword();
                    final String nodename = generateRandomNodename(firmware);

                    final DeviceDeployer deployer = _deviceService.getDeployer(device);
                    final FirmwareConfigurator configurator = _firmwareService.getConfigurator(firmware);

                    final File firmwareImage = _firmwareService.findFirmwareImage(firmwareImageDirectory, device, firmware);

                    if (firmwareImage == null) {
                        _lcdDriver.writeLines("No firmware", "found. :-(  [OK]");
                        waitForButton();
                        continue;
                    }

                    final DetailedDevice detailedDevice;
                    try {
                        _lcdDriver.writeLines("Installing...", "Please wait...");
                        detailedDevice = deployer.deploy(firmwareImage);
                    } catch (final FileNotFoundException e) {
                        throw new IllegalStateException("Could not deploy firmware.", e);
                    }
                    _lcdDriver.writeLines("Configuring...", "Please wait...");
                    final FirmwareConfiguration configuration = configurator.configure(password, nodename);

                    _labelPrintingService.printLabel(
                        detailedDevice,
                        configuration
                    );

                    _lcdDriver.writeLines("We are done...", "            [OK]");
                    waitForButton();
                }
            }
        }
        finally {
            try {
                _joystickDriver.shutdown();
            }
            finally {
                _lcdDriver.shutdown();
            }
        }
    }

    private Device detectDevice() {
        _lcdDriver.writeLines("Connect device", "            [OK]");
        waitForButton();

        Device device = null;
        while (device == null) {
            _lcdDriver.writeLines("Detecting...", "Please wait...");
            device = _deviceService.autodetectDevice();

            if (device == null) {
                _lcdDriver.writeLines("Detection failed", "Retry?     [Yes]");
                waitForButton();
            }
        }

        _lcdDriver.writeLines(device.asString(), "          [Next]");
        return device;
    }

    private Firmware chooseFirmware(final Collection<Firmware> deviceFirmwares) {
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
        Sleeper.sleepTight(100);
        _joystickDriver.flush();
        while (_joystickDriver.read() != BUTTON) {
        }
    }

    private String generateRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    private String generateRandomNodename(final Firmware firmware) {
        return firmware.getName() + "_" + RandomStringUtils.randomAlphanumeric(12);
    }
}
