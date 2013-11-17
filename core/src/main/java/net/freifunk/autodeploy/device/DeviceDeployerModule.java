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
