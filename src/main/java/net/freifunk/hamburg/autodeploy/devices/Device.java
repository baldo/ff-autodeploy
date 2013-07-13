package net.freifunk.hamburg.autodeploy.devices;

import com.google.common.base.Objects;

/**
 * A device having a model and hardware version.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class Device {

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
