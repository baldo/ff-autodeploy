package net.freifunk.hamburg.autodeploy.devices.tplink;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementSelectionStateToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.freifunk.hamburg.autodeploy.devices.Device;
import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Splitter;

/**
 * Deploys the Freifunk firmware to TP-Link devices.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public abstract class AbstractTPLinkDeployer implements DeviceDeployer {

    private static final String TP_LINK_WEB_INTERFACE_URL = "http://admin:admin@192.168.0.1";
    private static final String CONFIG_MODE_URL = "http://192.168.1.1";

    // menu
    private static final String MENU_FRAME_NAME = "bottomLeftFrame";

    // status page
    private static final By HARDWARE_VERSION = By.cssSelector("#hversion");

    // firmware upgrade page
    private static final String MAIN_FRAME_NAME = "mainFrame";

    private static final By FIRMWARE_FILE_CHOOSER = By.cssSelector("input[type=file]");
    private static final By FIRMWARE_UPGRADE_BUTTON = By.cssSelector("input[name=Upgrade]");

    private static final String EXPECTED_PROMPT_TEXT = "Are you sure to upgrade the firmware?";

    // config mode
    private static final By START_CONFIGURATION_LINK = By.cssSelector(".actions .btn.primary");
    private static final By NEXT_BUTTON = By.cssSelector(".actions .btn.primary");
    private static final By PASSWORD_FIELD1 = By.id("cbid.password.1.pw1");
    private static final By PASSWORD_FIELD2 = By.id("cbid.password.1.pw2");
    private static final By HOSTNAME_FIELD = By.id("cbid.hostname.1.hostname");
    private static final By MESH_VIA_VPN_CHECKBOX = By.id("cbid.meshvpn.1.meshvpn");
    private static final By VPN_KEY_CANDIDATES = By.cssSelector("#maincontent div");
    private static final By CONFIGURATION_HEADLINE = By.cssSelector("#maincontent h2");
    private static final By REBOOT_BUTTON = By.cssSelector(".btn.primary");
    private static final String CONFIGURATION_DONE_HEADLINE = "Konfiguration abgeschlossen";

    private final Set<Device> _supportedDevices;

    private final By _systemToolsMenuItem;
    private final By _firmwareUpgradeMenuItem;

    private final WebDriver _webDriver;
    private final WebDriverWait _wait;

    private final String _window;

    protected AbstractTPLinkDeployer(
        final Set<Device> supportedDevices,
        final By systemToolsMenuItem,
        final By firmwareUpgradeMenuItem,
        final WebDriver webDriver,
        final WebDriverWait wait
    ) {
        _supportedDevices = supportedDevices;

        _systemToolsMenuItem = systemToolsMenuItem;
        _firmwareUpgradeMenuItem = firmwareUpgradeMenuItem;

        _webDriver = webDriver;
        _wait = wait;

        _window = _webDriver.getWindowHandle();
    }

    private void selectFrame(final String frameName) {
        _webDriver.switchTo().window(_window);
        _wait.until(frameToBeAvailableAndSwitchToIt(frameName));
    }

    @Override
    public void deploy(final File firmwareImage, final String password, final String nodename) {
        goToWebInterface();
        ensureSupportedDevice();
        openFirmwareUpgradePage();
        startFirmwareUpgrade(firmwareImage);
        waitForReboot();

        goToConfigMode();
        startConfiguration();
        setPassword(password);
        setHostName(nodename);
        // TODO: Allow to disable VPN meshing.
        activateVPN();
        bootIntoRegularMode();
    }

    private void goToWebInterface() {
        _webDriver.navigate().to(TP_LINK_WEB_INTERFACE_URL);
    }

    private void ensureSupportedDevice() {
        selectFrame(MAIN_FRAME_NAME);
        _wait.until(presenceOfElementLocated(HARDWARE_VERSION));

        final String hardwareVersionString = _webDriver.findElement(HARDWARE_VERSION).getText();
        final Iterator<String> parts = Splitter.on(' ').trimResults().split(hardwareVersionString).iterator();

        final String model = parts.next();
        final String version = parts.next();

        final Device device = new Device(model, version);

        if (!_supportedDevices.contains(device)) {
            throw new IllegalStateException("Unsupported device: " + device);
        }
    }

    private void openFirmwareUpgradePage() {
        selectFrame(MENU_FRAME_NAME);

        _wait.until(elementToBeClickable(_systemToolsMenuItem));
        _webDriver.findElement(_systemToolsMenuItem).click();

        _wait.until(elementToBeClickable(_firmwareUpgradeMenuItem));
    }

    private void startFirmwareUpgrade(final File firmwareImage) {
        _webDriver.findElement(_firmwareUpgradeMenuItem).click();

        selectFrame(MAIN_FRAME_NAME);

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

    private void waitForReboot() {
        selectFrame(MAIN_FRAME_NAME);
        _wait
            .withTimeout(10, MINUTES)
        .until(textToBePresentInElement(By.cssSelector("#t_title"), "Restart"));
        try {
            Thread.sleep(20000 /* 20 seconds */);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Got interrupted while waiting for reboot.", e);
        }
    }

    private void goToConfigMode() {
        _webDriver.switchTo().window(_window);
        _webDriver.navigate().to(CONFIG_MODE_URL);
        _wait.until(titleContains("Freifunk"));
    }

    private void startConfiguration() {
        _wait.until(presenceOfElementLocated(START_CONFIGURATION_LINK));
        _webDriver.findElement(START_CONFIGURATION_LINK).click();
    }

    private void setPassword(final String password) {
        _wait.until(presenceOfElementLocated(PASSWORD_FIELD1));
        _webDriver.findElement(PASSWORD_FIELD1).sendKeys(password);

        _wait.until(presenceOfElementLocated(PASSWORD_FIELD2));
        _webDriver.findElement(PASSWORD_FIELD2).sendKeys(password);

        _wait.until(elementToBeClickable(NEXT_BUTTON));
        _webDriver.findElement(NEXT_BUTTON).click();
    }

    private void setHostName(final String hostname) {
        _wait.until(presenceOfElementLocated(HOSTNAME_FIELD));
        final WebElement hostnameField = _webDriver.findElement(HOSTNAME_FIELD);
        hostnameField.clear();
        hostnameField.sendKeys(hostname);

        _wait.until(elementToBeClickable(NEXT_BUTTON));
        _webDriver.findElement(NEXT_BUTTON).click();
    }

    private void activateVPN() {
        _wait.until(elementToBeClickable(MESH_VIA_VPN_CHECKBOX));
        final WebElement checkbox = _webDriver.findElement(MESH_VIA_VPN_CHECKBOX);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        _wait.until(elementSelectionStateToBe(MESH_VIA_VPN_CHECKBOX, true /* selected */));

        // TODO: Allow setting bandwidth limit.

        _wait.until(elementToBeClickable(NEXT_BUTTON));
        _webDriver.findElement(NEXT_BUTTON).click();

        getVPNKey();

        // TODO: Fill in webform.

        _wait.until(elementToBeClickable(NEXT_BUTTON));
        _webDriver.findElement(NEXT_BUTTON).click();
    }

    private String getVPNKey() {
        // We don't have a good selector for the VPN key, so we iterate over possible candidates multiple times.
        for (int i = 0; i < 5; i ++) {
            _wait.until(presenceOfAllElementsLocatedBy(VPN_KEY_CANDIDATES));
            final List<WebElement> candidates = _webDriver.findElements(VPN_KEY_CANDIDATES);
            String vpnKey = null;

            for (final WebElement candidate: candidates) {
                vpnKey = candidate.getText();
                if (Pattern.matches("[0-9a-f]{64}", vpnKey)) {
                    System.out.println("VPN key: " + vpnKey);
                    return vpnKey;
                }
            }
        }
        throw new IllegalStateException("Could not get VPN key.");
    }

    private void bootIntoRegularMode() {
        _wait.until(textToBePresentInElement(CONFIGURATION_HEADLINE, CONFIGURATION_DONE_HEADLINE));
        _wait.until(elementToBeClickable(REBOOT_BUTTON));
        _webDriver.findElement(REBOOT_BUTTON).click();
    }
}
