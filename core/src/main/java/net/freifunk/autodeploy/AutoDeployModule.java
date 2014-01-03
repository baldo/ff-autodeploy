/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
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
