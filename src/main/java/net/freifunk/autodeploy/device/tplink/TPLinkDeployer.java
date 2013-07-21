package net.freifunk.autodeploy.device.tplink;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceDeployer;
import net.freifunk.autodeploy.selenium.Actor;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Deploys the Freifunk firmware to TP-Link devices.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class TPLinkDeployer implements DeviceDeployer {

    public static final Set<Device> SUPPORTED_DEVICES = ImmutableSet.of(
        new Device("WDR3600", "v1"),
        new Device("WR741ND", "v4"),
        new Device("WR841N", "v8"),
        new Device("WR842ND", "v1")
    );

    private static final Logger LOG = LoggerFactory.getLogger(TPLinkDeployer.class);

    private static final String TP_LINK_WEB_INTERFACE_IP = "192.168.0.1";
    private static final String TP_LINK_WEB_INTERFACE_USER = "admin";
    private static final String TP_LINK_WEB_INTERFACE_PASSWORD = "admin";

    private static final String TP_LINK_WEB_INTERFACE_URL =
        "http://" + TP_LINK_WEB_INTERFACE_USER + ":" + TP_LINK_WEB_INTERFACE_PASSWORD + "@" + TP_LINK_WEB_INTERFACE_IP;

    // menu
    private static final String MENU_FRAME_NAME = "bottomLeftFrame";
    private static final By SYSTEM_TOOLS_MENU_ITEM = By.xpath("//a[contains(text(),'System Tools')]");
    private static final By FIRMWARE_UPGRADE_MENU_ITEM = By.xpath("//a[contains(text(),'Firmware Upgrade')]");

    // status page
    private static final By HARDWARE_VERSION = By.id("hversion");

    // firmware upgrade page
    private static final String MAIN_FRAME_NAME = "mainFrame";

    private static final By FIRMWARE_FILE_CHOOSER = By.cssSelector("input[type=file]");
    private static final By FIRMWARE_UPGRADE_BUTTON = By.cssSelector("input[name=Upgrade]");

    private final Actor _actor;

    @Inject
    public TPLinkDeployer(
        final Actor actor
    ) {
        _actor = actor;
    }

    @Override
    public void deploy(final File firmwareImage) {
        LOG.info("Starting deployment: firmware = {}", firmwareImage);
        goToWebInterface();
        ensureSupportedDevice();
        openFirmwareUpgradePage();
        startFirmwareUpgrade(firmwareImage);
        waitForReboot();
    }

    private void goToWebInterface() {
        _actor.navigateTo(TP_LINK_WEB_INTERFACE_URL);
    }

    private void ensureSupportedDevice() {
        _actor.selectFrame(MAIN_FRAME_NAME);
        final String hardwareVersionString = _actor.getTextOfElement(HARDWARE_VERSION);

        final Device device = hardwareVersionToDevice(hardwareVersionString);

        if (!SUPPORTED_DEVICES.contains(device)) {
            throw new IllegalStateException("Unsupported device: " + device);
        }
    }

    private Device hardwareVersionToDevice(final String hardwareVersionString) {
        final Iterator<String> parts = Splitter.on(' ').trimResults().split(hardwareVersionString).iterator();
        final String model = parts.next();
        final String version = parts.next();

        final Device device = new Device(model, version);
        return device;
    }

    private void openFirmwareUpgradePage() {
        _actor.selectFrame(MENU_FRAME_NAME);
        _actor.clickElement(SYSTEM_TOOLS_MENU_ITEM);
        _actor.clickElement(FIRMWARE_UPGRADE_MENU_ITEM);
    }

    private void startFirmwareUpgrade(final File firmwareImage) {
        _actor.selectFrame(MAIN_FRAME_NAME);
        _actor.chooseFile(FIRMWARE_FILE_CHOOSER, firmwareImage);

        // disable checking of firmware as this is not very reliable and requires unnecessarily complicated handling
        _actor.executeJavascript("doSubmit = function () { return true; }");

        _actor.clickElement(FIRMWARE_UPGRADE_BUTTON);
    }

    private void waitForReboot() {
        _actor.selectFrame(MAIN_FRAME_NAME);
        _actor.waitForElementContainingText(By.cssSelector("#t_title"), "Restart", 10, MINUTES);
        try {
            Thread.sleep(20000 /* 20 seconds */);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Got interrupted while waiting for reboot.", e);
        }
    }
}
