package net.freifunk.autodeploy;

import static com.google.inject.Scopes.SINGLETON;

import java.util.Set;

import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceDeployer;
import net.freifunk.autodeploy.device.DeviceService;
import net.freifunk.autodeploy.device.DeviceServiceImpl;
import net.freifunk.autodeploy.device.tplink.TPLinkDeployer;
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareConfigurator;
import net.freifunk.autodeploy.firmware.FirmwareService;
import net.freifunk.autodeploy.firmware.FirmwareServiceImpl;
import net.freifunk.autodeploy.firmware.FreifunkHamburgConfigurator;
import net.freifunk.autodeploy.firmware.FreifunkKielConfigurator;
import net.freifunk.autodeploy.selenium.Actor;
import net.freifunk.autodeploy.selenium.ActorImpl;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

/**
 * Main {@link Module} for binding everything together.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class AutoDeployModule extends AbstractModule {

    @SuppressWarnings("unchecked")
    private void bindDeployer(final Class<? extends DeviceDeployer> cls) {
        final MapBinder<Device, DeviceDeployer> deployerBinder = MapBinder.newMapBinder(binder(), Device.class, DeviceDeployer.class);
        try {
            for (final Device device: (Set<Device>) cls.getField("SUPPORTED_DEVICES").get(null)) {
                deployerBinder.addBinding(device).to(cls).in(SINGLETON);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind deployer: " + cls, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindConfigurator(final Class<? extends FirmwareConfigurator> cls) {
        final MapBinder<Firmware, FirmwareConfigurator> configuratorBinder = MapBinder.newMapBinder(binder(), Firmware.class, FirmwareConfigurator.class);
        try {
            for (final Firmware firmware: (Set<Firmware>) cls.getField("SUPPORTED_FIRMWARES").get(null)) {
                configuratorBinder.addBinding(firmware).to(cls).in(SINGLETON);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind configurator: " + cls, e);
        }
    }

    @Override
    protected void configure() {
        bindDeployer(TPLinkDeployer.class);

        bindConfigurator(FreifunkHamburgConfigurator.class);
        bindConfigurator(FreifunkKielConfigurator.class);

        bind(Actor.class).to(ActorImpl.class).in(SINGLETON);
        bind(DeviceService.class).to(DeviceServiceImpl.class).in(SINGLETON);
        bind(FirmwareService.class).to(FirmwareServiceImpl.class).in(SINGLETON);
    }

    @Provides
    @Singleton
    private WebDriver provideWebDriver() {
        final HtmlUnitDriver webDriver = new HeadlessDriver();
//        final FirefoxDriver webDriver = new FirefoxDriver();
        return webDriver;
    }

    @Provides
    @Singleton
    private WebDriverWait provideWebDriverWait(final WebDriver webDriver) {
        final WebDriverWait webDriverWait = new WebDriverWait(webDriver, 10 /* seconds */);
        return webDriverWait;
    }
}
