package net.freifunk.hamburg.autodeploy;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.hamburg.autodeploy.devices.DeviceDeployer;
import net.freifunk.hamburg.autodeploy.devices.tplink.WR842NDDeployer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Main {@link Module} for binding everything together.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class AutoDeployModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeviceDeployer.class).to(WR842NDDeployer.class).in(SINGLETON);
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
        final WebDriverWait webDriverWait = new WebDriverWait(webDriver, 5 /* seconds */);
        return webDriverWait;
    }
}
