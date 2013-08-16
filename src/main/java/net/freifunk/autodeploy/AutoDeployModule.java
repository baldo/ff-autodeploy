package net.freifunk.autodeploy;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;

import java.util.Set;

import net.freifunk.autodeploy.device.Device;
import net.freifunk.autodeploy.device.DeviceDeployer;
import net.freifunk.autodeploy.device.tplink.TPLinkDeployer;
import net.freifunk.autodeploy.firmware.Firmware;
import net.freifunk.autodeploy.firmware.FirmwareConfigurator;
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
import com.google.inject.multibindings.Multibinder;

/**
 * Main {@link Module} for binding everything together.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class AutoDeployModule extends AbstractModule {

    @SuppressWarnings("unchecked")
    private void bindDeployer(final Multibinder<Device> deviceBinder, final Class<? extends DeviceDeployer> cls) {
        try {

            for (final Device device: (Set<Device>) cls.getField("SUPPORTED_DEVICES").get(null)) {
                deviceBinder.addBinding().toInstance(device);
                bind(DeviceDeployer.class).annotatedWith(named(device.asString())).to(cls).in(SINGLETON);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind deployer: " + cls, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindConfigurator(final Multibinder<Firmware> firmwareBinder, final Class<? extends FirmwareConfigurator> cls) {
        try {
            for (final Firmware firmware: (Set<Firmware>) cls.getField("SUPPORTED_FIRMWARES").get(null)) {
                firmwareBinder.addBinding().toInstance(firmware);
                bind(FirmwareConfigurator.class).annotatedWith(named(firmware.getName())).to(cls).in(SINGLETON);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind configurator: " + cls, e);
        }
    }

    @Override
    protected void configure() {
        final Multibinder<Device> deviceBinder = Multibinder.newSetBinder(binder(), Device.class);
        bindDeployer(deviceBinder, TPLinkDeployer.class);

        final Multibinder<Firmware> firmwareBinder = Multibinder.newSetBinder(binder(), Firmware.class);
        bindConfigurator(firmwareBinder, FreifunkHamburgConfigurator.class);
        bindConfigurator(firmwareBinder, FreifunkKielConfigurator.class);

        bind(Actor.class).to(ActorImpl.class).in(SINGLETON);
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
