package net.freifunk.hamburg.autodeploy.devices.tplink;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;
import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Deploys the Freifunk firmware to the WR842ND.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class WR842NDDeployer implements DeviceDeployer {

    private static final Set<Device> SUPPORTED_DEVICES = ImmutableSet.of(new Device("WR842ND", "v1"));

    private static final String WEB_INTERFACE_URL = "http://admin:admin@192.168.0.1";
    private static final String WINDOW_NAME = "";

    // menu
    private static final String MENU_FRAME_NAME = "bottomLeftFrame";
    private static final By SYSTEM_TOOLS_MENU_ITEM = By.cssSelector("#a53");
    private static final By FIRMWARE_UPGRADE_MENU_ITEM = By.cssSelector("#a56");

    // status page
    private static final By HARDWARE_VERSION = By.cssSelector("#hversion");

    // firmware upgrade page
    private static final String MAIN_FRAME_NAME = "mainFrame";
    private static final By FIRMWARE_FILE_CHOOSER = By.cssSelector("input[type=file]");
    private static final By FIRMWARE_UPGRADE_BUTTON = By.cssSelector("input[name=Upgrade]");

    private static final String EXPECTED_PROMPT_TEXT = "Are you sure to upgrade the firmware?";

    private final WebDriver _webDriver;
    private final WebDriverWait _wait;

    @Inject
    public WR842NDDeployer(
        final WebDriver webDriver,
        final WebDriverWait wait
    ) {
        _webDriver = webDriver;
        _wait = wait;

    }

    private void selectFrame(final String frameName) {
        _webDriver.switchTo().window(WINDOW_NAME);
        _webDriver.switchTo().frame(frameName);
    }

    @Override
    public void deploy(final File firmwareImage) {
        goToWebInterface();
        ensureSupportedDevice();
        openFirmwareUpgradePage();
        startFirmwareUpgrade(firmwareImage);
        waitForStartOfReboot();
    }

    private void goToWebInterface() {
        _webDriver.navigate().to(WEB_INTERFACE_URL);
    }

    private void ensureSupportedDevice() {
        selectFrame(MAIN_FRAME_NAME);
        _wait.until(ExpectedConditions.presenceOfElementLocated(HARDWARE_VERSION));

        final String hardwareVersionString = _webDriver.findElement(HARDWARE_VERSION).getText();
        final Iterator<String> parts = Splitter.on(' ').trimResults().split(hardwareVersionString).iterator();

        final String model = parts.next();
        final String version = parts.next();

        final Device device = new Device(model, version);

        if (!SUPPORTED_DEVICES.contains(device)) {
            throw new IllegalStateException("Unsupported device: " + device);
        }
    }

    private void openFirmwareUpgradePage() {
        selectFrame(MENU_FRAME_NAME);

        _wait.until(elementToBeClickable(SYSTEM_TOOLS_MENU_ITEM));
        _webDriver.findElement(SYSTEM_TOOLS_MENU_ITEM).click();

        _wait.until(elementToBeClickable(FIRMWARE_UPGRADE_MENU_ITEM));
    }

    private void startFirmwareUpgrade(final File firmwareImage) {
        _webDriver.findElement(FIRMWARE_UPGRADE_MENU_ITEM).click();

        _webDriver.switchTo().window(WINDOW_NAME);
        _webDriver.switchTo().frame(MAIN_FRAME_NAME);

        _wait.until(elementToBeClickable(FIRMWARE_UPGRADE_BUTTON));
        _webDriver.findElement(FIRMWARE_FILE_CHOOSER).sendKeys(firmwareImage.getAbsoluteFile().getPath());
        _webDriver.findElement(FIRMWARE_UPGRADE_BUTTON).click();

        final Alert prompt = _webDriver.switchTo().alert();
        final String promptText = prompt.getText();

        if (!EXPECTED_PROMPT_TEXT.equals(promptText)) {
            prompt.dismiss();
            throw new IllegalStateException("Unexpected prompt message: " + promptText);
        }

        prompt.accept();
    }

    private void waitForStartOfReboot() {
        _webDriver.switchTo().window(WINDOW_NAME);
        _webDriver.switchTo().frame(MAIN_FRAME_NAME);

        _wait.withTimeout(2, MINUTES).until(textToBePresentInElement(By.cssSelector("#t_title"), "Restart"));
    }
}
