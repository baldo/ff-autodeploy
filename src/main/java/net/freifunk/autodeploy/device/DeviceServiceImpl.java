package net.freifunk.autodeploy.device;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * Default implementation of {@link DeviceService}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class DeviceServiceImpl implements DeviceService {

    private final Map<Device, DeviceDeployer> _deployers;

    @Inject
    public DeviceServiceImpl(
        final Map<Device, DeviceDeployer> deployers
    ) {
        _deployers = deployers;
    }

    @Override
    public Device findSupportedDevice(final String deviceString) {
        final Iterable<Device> matches = Iterables.filter(_deployers.keySet(), new Predicate<Device>() {

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
        final DeviceDeployer deployer = _deployers.get(device);
        if (deployer == null) {
            throw new IllegalArgumentException("No deployer found for device: " + device);
        }
        return deployer;
    }

    @Override
    public Set<Device> getSupportedDevices() {
        return _deployers.keySet();
    }
}
