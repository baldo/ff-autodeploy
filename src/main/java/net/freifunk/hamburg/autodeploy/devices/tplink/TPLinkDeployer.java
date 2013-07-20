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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
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

    private static final String CONFIG_MODE_URL = "http://192.168.1.1";

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

    private final WebDriver _webDriver;
    private final WebDriverWait _wait;

    private final String _window;

    @Inject
    public TPLinkDeployer(
        final WebDriver webDriver,
        final WebDriverWait wait
    ) {
        _webDriver = webDriver;
        _wait = wait;

        _window = _webDriver.getWindowHandle();
    }

    private void switchToWindow() {
        LOG.debug("switchToWindow: {}", _window);
        _webDriver.switchTo().window(_window);
        LOG.debug("switchToWindow done: {}", _window);
    }

    private void navigateTo(final String url) {
        LOG.debug("navigateTo: {}", url);
        switchToWindow();
        _webDriver.navigate().to(url);
        LOG.debug("navigateTo done: {}", url);
    }

    private void selectFrame(final String frameName) {
        LOG.debug("selectFrame: {}", frameName);
        switchToWindow();
        _wait.until(frameToBeAvailableAndSwitchToIt(frameName));
        LOG.debug("selectFrame done: {}", frameName);
    }

    private void waitForElement(final By by) {
        LOG.debug("waitForElement: {}", by);
        _wait.until(presenceOfElementLocated(by));
        LOG.debug("waitForElement done: {}", by);
    }

    private void waitForClickableElement(final By by) {
        LOG.debug("waitForClickableElement: {}", by);
        _wait.until(elementToBeClickable(by));
        LOG.debug("waitForClickableElement done: {}", by);
    }

    private void waitForTitleContaining(final String substring) {
        LOG.debug("waitForTitleContaining: {}", substring);
        _wait.until(titleContains(substring));
        LOG.debug("waitForTitleContaining done: {}", substring);
    }

    private WebElement getElement(final By by) {
        LOG.debug("getElement: {}", by);
        waitForElement(by);
        final WebElement element = _webDriver.findElement(by);
        LOG.debug("getElement done: {}", by);
        return element;
    }

    private String getTextOfElement(final By by) {
        LOG.debug("getTextOfElement: {}", by);
        final WebElement element = getElement(by);
        final String text = element.getText();
        LOG.debug("getTextOfElement done: {} => {}", by, text);
        return text;
    }

    private void clickElement(final By by) {
        LOG.debug("clickElement: {}", by);
        waitForClickableElement(by);
        final WebElement element = getElement(by);
        element.click();
        LOG.debug("clickElement done: {}", by);
    }

    private void chooseFile(final By by, final File file) {
        LOG.debug("chooseFile: {}, {}", by, file);
        final WebElement element = getElement(by);
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "file".equals(element.getAttribute("type")),
            "Element should be a file input: {}",
            element
        );
        element.sendKeys(file.getAbsoluteFile().getPath());
        LOG.debug("chooseFile done: {}, {}", by, file);
    }

    private void typeIntoTextInput(final By by, final String text) {
        LOG.debug("typeIntoTextInput: {}, {}", by, text);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "text".equals(element.getAttribute("type")),
            "Element should be a text input: {}",
            element
        );
        element.sendKeys(text);
        LOG.debug("typeIntoTextInput done: {}, {}", by, text);
    }

    private void typeIntoPasswordInput(final By by, final String password) {
        LOG.debug("typeIntoPasswordInput: {}, ********", by);
        final WebElement element = getElement(by);
        element.clear();
        Preconditions.checkState(
            "input".equals(element.getTagName()) && "password".equals(element.getAttribute("type")),
            "Element should be a password input: {}",
            element
        );
        element.sendKeys(password);
        LOG.debug("typeIntoPasswordInput done: {}, ********", by);
    }

    private void updateCheckbox(final By by, final boolean checked) {
        LOG.debug("updateCheckbox: {}, {}", by, checked);
        waitForClickableElement(by);
        final WebElement checkbox = getElement(by);
        Preconditions.checkState(
            "input".equals(checkbox.getTagName()) && "checkbox".equals(checkbox.getAttribute("type")),
            "Element should be a checkbox: {}",
            checkbox
        );
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
        _wait.until(elementSelectionStateToBe(MESH_VIA_VPN_CHECKBOX, checked));
        LOG.debug("updateCheckbox done: {}, {}", by, checked);
    }

    private void executeJavascript(final String js) {
        LOG.debug("executeJavascript: {}", js);
        final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) _webDriver;
        javascriptExecutor.executeScript(js);
        LOG.debug("executeJavascript done: {}", js);
    }

    @Override
    public void deploy(final File firmwareImage, final String password, final String nodename) {
        LOG.info("Starting deployment: firmware = {}, nodename = {}", firmwareImage, nodename);
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
        navigateTo(TP_LINK_WEB_INTERFACE_URL);
    }

    private void ensureSupportedDevice() {
        selectFrame(MAIN_FRAME_NAME);
        final String hardwareVersionString = getTextOfElement(HARDWARE_VERSION);

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
        selectFrame(MENU_FRAME_NAME);
        clickElement(SYSTEM_TOOLS_MENU_ITEM);
        clickElement(FIRMWARE_UPGRADE_MENU_ITEM);
    }

    private void startFirmwareUpgrade(final File firmwareImage) {
        selectFrame(MAIN_FRAME_NAME);
        chooseFile(FIRMWARE_FILE_CHOOSER, firmwareImage);

        // disable checking of firmware as this is not very reliable and requires unnecessarily complicated handling
        executeJavascript("doSubmit = function () { return true; }");

        clickElement(FIRMWARE_UPGRADE_BUTTON);
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
        switchToWindow();
        navigateTo(CONFIG_MODE_URL);
        waitForTitleContaining("Freifunk");
    }

    private void startConfiguration() {
        clickElement(START_CONFIGURATION_LINK);
    }

    private void setPassword(final String password) {
        typeIntoPasswordInput(PASSWORD_FIELD1, password);
        typeIntoPasswordInput(PASSWORD_FIELD2, password);
        clickElement(NEXT_BUTTON);
    }

    private void setHostName(final String hostname) {
        typeIntoTextInput(HOSTNAME_FIELD, hostname);
        clickElement(NEXT_BUTTON);
    }

    private void activateVPN() {
        updateCheckbox(MESH_VIA_VPN_CHECKBOX, true);
        // TODO: Allow setting bandwidth limit.
        clickElement(NEXT_BUTTON);

        getVPNKey();
        // TODO: Fill in webform.
        clickElement(NEXT_BUTTON);
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
        clickElement(REBOOT_BUTTON);
    }
}
