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

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * Default implementation of {@link DeviceService}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class DeviceServiceImpl implements DeviceService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);

    private final Map<Device, DeviceDeployer> _deployersByDevice;
    private final Set<DeviceDeployer> _deployers;

    @Inject
    public DeviceServiceImpl(
        final Map<Device, DeviceDeployer> deployersByDevice
    ) {
        _deployersByDevice = deployersByDevice;
        _deployers = ImmutableSet.copyOf(deployersByDevice.values());
    }

    @Override
    public Device autodetectDevice() {
        for (final DeviceDeployer deployer: _deployers) {
            try {
                LOG.debug("Trying to dectect device with: {}", deployer.getClass().getSimpleName());
                final Device device = deployer.autodetect();

                if (device != null) {
                    LOG.debug("Detected device: {}", device);
                    return device;
                }
            } catch (final Throwable t) {
                LOG.warn("Detecting device with deployer " + deployer.getClass().getSimpleName() + " failed.", t);
            }
        }

        return null;
    }

    @Override
    public Device findSupportedDevice(final String deviceString) {
        final Iterable<Device> matches = Iterables.filter(_deployersByDevice.keySet(), new Predicate<Device>() {

            @Override
            public boolean apply(final Device device) {
                return device != null && device.asString().equals(deviceString);
            }
        });

        if (Iterables.size(matches) > 1) {
            throw new IllegalStateException("More than one device found: " + deviceString);
        }
        return Iterables.getFirst(matches, null);
    }

    @Override
    public DeviceDeployer getDeployer(final Device device) {
        final DeviceDeployer deployer = _deployersByDevice.get(device);
        if (deployer == null) {
            throw new IllegalArgumentException("No deployer found for device: " + device);
        }
        return deployer;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return _deployersByDevice.keySet();
    }
}
