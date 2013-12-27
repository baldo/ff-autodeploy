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
