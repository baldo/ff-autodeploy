package net.freifunk.hamburg.autodeploy;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;

import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;
import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;
import net.freifunk.hamburg.autodeploy.devices.tplink.TPLinkDeployer;

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

    @Override
    protected void configure() {
        final Multibinder<Device> deviceBinder = Multibinder.newSetBinder(binder(), Device.class);
        bindDeployer(deviceBinder, TPLinkDeployer.class);
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
