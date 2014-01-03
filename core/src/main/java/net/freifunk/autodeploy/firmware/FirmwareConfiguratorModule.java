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
package net.freifunk.autodeploy.firmware;

import static com.google.inject.Scopes.SINGLETON;

import java.util.Map;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

/**
 * {@link Module} to bind a {@link FirmwareConfigurator}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class FirmwareConfiguratorModule extends PrivateModule {

    @Override
    protected final void configure() {
        bindConfigurator(FreifunkHamburgConfigurator.class);
        bindConfigurator(FreifunkKielConfigurator.class);
        bindConfigurator(FreifunkLuebeckConfigurator.class);
        bindConfigurator(FreifunkLueneburgConfigurator.class);

        expose(new TypeLiteral<Map<Firmware, FirmwareConfigurator>>() {});
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
}
