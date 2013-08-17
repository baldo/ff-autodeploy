package net.freifunk.autodeploy.firmware;

import net.freifunk.autodeploy.selenium.Actor;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the Freifunk Nord firmware.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public abstract class AbstractFreifunkNordConfigurator implements FirmwareConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFreifunkNordConfigurator.class);

    // config mode
    private static final String CONFIG_MODE_URL = "http://192.168.1.1";

    private static final By START_CONFIGURATION_LINK = By.cssSelector(".actions .btn.primary");
    private static final By NEXT_BUTTON = By.cssSelector(".actions .btn.primary");
    private static final By PASSWORD_FIELD1 = By.id("cbid.password.1.pw1");
    private static final By PASSWORD_FIELD2 = By.id("cbid.password.1.pw2");
    private static final By HOSTNAME_FIELD = By.id("cbid.hostname.1.hostname");
    private static final By MESH_VIA_VPN_CHECKBOX = By.id("cbid.meshvpn.1.meshvpn");
    private static final By VPN_KEY = By.cssSelector("#maincontent div"); // pretty fragile, but works for now
    private static final By CONFIGURATION_HEADLINE = By.cssSelector("#maincontent h2");
    private static final By REBOOT_BUTTON = By.cssSelector(".btn.primary");
    private static final String CONFIGURATION_DONE_HEADLINE = "Konfiguration abgeschlossen";

    private final Actor _actor;

    public AbstractFreifunkNordConfigurator(
        final Actor actor
    ) {
        _actor = actor;
    }

    @Override
    public void configure(final String password, final String nodename) {
        goToConfigMode();
        startConfiguration();
        setPassword(password);
        setHostName(nodename);
        // TODO: Allow to disable VPN meshing.
        activateVPN();
        bootIntoRegularMode();
    }

    private void goToConfigMode() {
        _actor.switchToWindow();
        _actor.navigateTo(CONFIG_MODE_URL);
        _actor.waitForTitleContaining("LuCI");
    }

    private void startConfiguration() {
        _actor.clickElement(START_CONFIGURATION_LINK);
    }

    private void setPassword(final String password) {
        _actor.typeIntoPasswordInput(PASSWORD_FIELD1, password);
        _actor.typeIntoPasswordInput(PASSWORD_FIELD2, password);
        _actor.clickElement(NEXT_BUTTON);
    }

    private void setHostName(final String hostname) {
        _actor.typeIntoTextInput(HOSTNAME_FIELD, hostname);
        _actor.clickElement(NEXT_BUTTON);
    }

    private void activateVPN() {
        _actor.updateCheckbox(MESH_VIA_VPN_CHECKBOX, true);
        // TODO: Allow setting bandwidth limit.
        _actor.clickElement(NEXT_BUTTON);

        getVPNKey();
        _actor.clickElement(NEXT_BUTTON);
        // TODO: Do community specific configuration.
    }

    private String getVPNKey() {
        final String vpnKey = _actor.getTextOfElement(VPN_KEY);
        LOG.info("VPN key: {}", vpnKey);
        return vpnKey;
    }

    private void bootIntoRegularMode() {
        _actor.waitForElementContainingText(CONFIGURATION_HEADLINE, CONFIGURATION_DONE_HEADLINE);
        _actor.clickElement(REBOOT_BUTTON);
    }
}
