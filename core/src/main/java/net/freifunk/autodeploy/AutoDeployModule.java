package net.freifunk.autodeploy;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.autodeploy.device.DeviceDeployerModule;
import net.freifunk.autodeploy.device.DeviceService;
import net.freifunk.autodeploy.device.DeviceServiceImpl;
import net.freifunk.autodeploy.firmware.FirmwareConfiguratorModule;
import net.freifunk.autodeploy.firmware.FirmwareService;
import net.freifunk.autodeploy.firmware.FirmwareServiceImpl;
import net.freifunk.autodeploy.selenium.Actor;
import net.freifunk.autodeploy.selenium.ActorImpl;
import net.freifunk.autodeploy.selenium.HeadlessDriver;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(AutoDeployModule.class);

    @Override
    protected void configure() {
        install(new DeviceDeployerModule());
        install(new FirmwareConfiguratorModule());

        bind(Actor.class).to(ActorImpl.class).in(SINGLETON);
        bind(DeviceService.class).to(DeviceServiceImpl.class).in(SINGLETON);
        bind(FirmwareService.class).to(FirmwareServiceImpl.class).in(SINGLETON);
    }

    @Provides
    @Singleton
    private WebDriver provideWebDriver() {
        final WebDriver webDriver;

        if ("true".equals(System.getProperty("webdriver.firefox.enable"))) {
            try {
                final ClassLoader classLoader = this.getClass().getClassLoader();
                @SuppressWarnings("unchecked")
                final Class<? extends WebDriver> firefoxDriverClass = (Class<? extends WebDriver>) classLoader.loadClass(
                    "org.openqa.selenium.firefox.FirefoxDriver"
                );
                final WebDriver firefoxDriver = firefoxDriverClass.newInstance();
                webDriver = firefoxDriver;
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Trying to instantiate FirefoxDriver failed. Did you compile with -Dwebdriver.firefox.allow=true?", e);
            }
        } else {
            final HtmlUnitDriver htmlUnitDriver = new HeadlessDriver();
            webDriver = htmlUnitDriver;
        }

        LOG.debug("WebDriver being used: " + webDriver.getClass().getSimpleName());
        return webDriver;
    }

    @Provides
    @Singleton
    private WebDriverWait provideWebDriverWait(final WebDriver webDriver) {
        final WebDriverWait webDriverWait = new WebDriverWait(webDriver, 10 /* seconds */);
        return webDriverWait;
    }

    @Provides
    @Singleton
    private HttpClient provideHttpClient() {
        return HttpClientBuilder.create().build();
    }

    @Provides
    @Singleton
    private ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }
}
