package net.freifunk.hamburg.autodeploy.devices.tplink;

import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Deploys the Freifunk firmware to the WR741ND.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class WR741NDDeployer extends AbstractTPLinkDeployer {

    public static final String MODEL_NAME = "WR741ND";
    public static final Set<Device> SUPPORTED_DEVICES = ImmutableSet.of(
        new Device(MODEL_NAME, "v4")
    );

    @Inject
    public WR741NDDeployer(final WebDriver webDriver, final WebDriverWait wait) {
        super(
            SUPPORTED_DEVICES,
            webDriver,
            wait
        );
    }
}
