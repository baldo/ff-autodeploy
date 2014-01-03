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
package net.freifunk.autodeploy.device;

import static com.google.inject.Scopes.SINGLETON;

import java.util.Map;
import java.util.Set;

import net.freifunk.autodeploy.device.tplink.TPLinkDeployer;

import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

/**
 * {@link Module} to bind a {@link DeviceDeployer}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class DeviceDeployerModule extends PrivateModule {

    @Override
    protected final void configure() {
        bindDeployer(TPLinkDeployer.class);

        expose(new TypeLiteral<Map<Device, DeviceDeployer>>() {});
    }

    @SuppressWarnings("unchecked")
    private void bindDeployer(final Class<? extends DeviceDeployer> cls) {
        bind(cls).in(SINGLETON);

        final MapBinder<Device, DeviceDeployer> deployerByDeviceBinder = MapBinder.newMapBinder(binder(), Device.class, DeviceDeployer.class);
        try {
            for (final Device device: (Set<Device>) cls.getField("SUPPORTED_DEVICES").get(null)) {
                deployerByDeviceBinder.addBinding(device).to(cls);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Could not bind deployer: " + cls, e);
        }
    }
}
