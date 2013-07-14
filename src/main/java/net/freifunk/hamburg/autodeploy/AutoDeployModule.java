package net.freifunk.hamburg.autodeploy;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;

import java.util.Set;

import net.freifunk.hamburg.autodeploy.devices.Device;
import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;
import net.freifunk.hamburg.autodeploy.devices.tplink.WR841NDeployer;
import net.freifunk.hamburg.autodeploy.devices.tplink.WR842NDDeployer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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
            bind(DeviceDeployer.class).annotatedWith(named((String) cls.getField("MODEL_NAME").get(null))).to(cls).in(SINGLETON);

            for (final Device device: (Set<Device>) cls.getField("SUPPORTED_DEVICES").get(null)) {
                deviceBinder.addBinding().toInstance(device);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind deployer: " + cls, e);
        }
    }

    @Override
    protected void configure() {
        final Multibinder<Device> deviceBinder = Multibinder.newSetBinder(binder(), Device.class);
        bindDeployer(deviceBinder, WR841NDeployer.class);
        bindDeployer(deviceBinder, WR842NDDeployer.class);
    }

    @Provides
    @Singleton
    private WebDriver provideWebDriver() {
        final FirefoxDriver webDriver = new FirefoxDriver();
        return webDriver;
    }

    @Provides
    @Singleton
    private WebDriverWait provideWebDriverWait(final WebDriver webDriver) {
        final WebDriverWait webDriverWait = new WebDriverWait(webDriver, 10 /* seconds */);
        return webDriverWait;
    }
}
