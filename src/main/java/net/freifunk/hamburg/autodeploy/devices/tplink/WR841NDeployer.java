package net.freifunk.hamburg.autodeploy.devices.tplink;

import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Deploys the Freifunk firmware to the WR841N.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class WR841NDeployer extends AbstractTPLinkDeployer {

    public static final String MODEL_NAME = "WR841N";
    public static final Set<Device> SUPPORTED_DEVICES = ImmutableSet.of(
        new Device(MODEL_NAME, "v8")
    );

    private static final By SYSTEM_TOOLS_MENU_ITEM = By.cssSelector("#a43");
    private static final By FIRMWARE_UPGRADE_MENU_ITEM = By.cssSelector("#a46");

    @Inject
    public WR841NDeployer(final WebDriver webDriver, final WebDriverWait wait) {
        super(
            SUPPORTED_DEVICES,

            SYSTEM_TOOLS_MENU_ITEM,
            FIRMWARE_UPGRADE_MENU_ITEM,

            webDriver,
            wait
        );
    }
}
