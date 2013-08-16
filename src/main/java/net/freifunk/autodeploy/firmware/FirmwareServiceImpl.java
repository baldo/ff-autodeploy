package net.freifunk.autodeploy.firmware;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class FirmwareServiceImpl implements FirmwareService {

    private final Map<Firmware, FirmwareConfigurator> _configurators;

    @Inject
    public FirmwareServiceImpl(
        final Map<Firmware, FirmwareConfigurator> configurators
    ) {
        _configurators = configurators;
    }

    @Override
    public Firmware findSupportedFirmware(final String firmwareString) {
        final Iterable<Firmware> matches = Iterables.filter(_configurators.keySet(), new Predicate<Firmware>() {

            @Override
            public boolean apply(final Firmware firmware) {
                return firmware != null && firmware.getName().equals(firmwareString);
            }
        });

        if (Iterables.size(matches) > 1) {
            throw new IllegalStateException("More than one firmware found: " + firmwareString);
        }
        return Iterables.getFirst(matches, null);
    }

    @Override
    public FirmwareConfigurator getConfigurator(final Firmware firmware) {
        final FirmwareConfigurator configurator = _configurators.get(firmware);
        if (configurator == null) {
            throw new IllegalArgumentException("No configurator found for firmware: " + firmware);
        }
        return configurator;
    }

    @Override
    public Set<Firmware> getSupportedFirmwares() {
        return _configurators.keySet();
    }
}
