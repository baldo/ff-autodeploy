package net.freifunk.autodeploy.device;


/**
 * Detailed information about a specific device.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class DetailedDevice {

    private final Device _device;
    private final String _mac;

    public DetailedDevice(final Device device, final String mac) {
        _device = device;
        _mac = mac;
    }

    public Device getDevice() {
        return _device;
    }

    public String getMac() {
        return _mac;
    }
}
