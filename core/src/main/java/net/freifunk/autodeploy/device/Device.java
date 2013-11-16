package net.freifunk.autodeploy.device;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * A device having a model and hardware version.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class Device implements Comparable<Device> {

    private final String _model;
    private final String _version;

    public Device(final String model, final String version) {
        _model = model;
        _version = version;
    }

    public String getModel() {
        return _model;
    }

    public String getVersion() {
        return _version;
    }

    @Override
    public int compareTo(final Device other) {
        return ComparisonChain.start()
            .compare(this._model, other._model)
            .compare(this._version, other._version)
        .result();
    }

    public String asString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(_model);
        builder.append('-');
        builder.append(_version);
        return builder.toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Device) {
            final Device otherDevice = (Device) other;
            return Objects.equal(_model, otherDevice._model) && Objects.equal(_version, otherDevice._version);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_model, _version);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("model", _model).add("version", _version).toString();
    }
}