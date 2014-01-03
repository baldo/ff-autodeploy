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

import java.net.URI;

import net.freifunk.autodeploy.device.Device;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;

/**
 * Represents the firmware of a specific community.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class Firmware implements Comparable<Firmware> {

    private final String _name;
    private final String _displayName;
    private final URI _firmwareUri;

    public Firmware(final String name, final String displayName, final URI firmwareUri) {
        _name = name;
        _displayName = displayName;
        _firmwareUri = firmwareUri;
    }

    public String getName() {
        return _name;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public URI getFirmwareUri() {
        return _firmwareUri;
    }

    @Override
    public int compareTo(final Firmware other) {
        return ComparisonChain.start()
            .compare(this._name, other._name)
        .result();
    }

    public static Device fromString(final String deviceString) {
        final Iterable<String> parts = Splitter.on('-').split(deviceString);
        Preconditions.checkArgument(Iterables.size(parts) == 2);
        final String model = Iterables.get(parts, 0);
        final String version = Iterables.get(parts, 1);
        return new Device(model, version);
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Firmware) {
            final Firmware otherFirmware = (Firmware) other;
            return Objects.equal(_name, otherFirmware._name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", _name).add("displayName", _displayName).toString();
    }
}
