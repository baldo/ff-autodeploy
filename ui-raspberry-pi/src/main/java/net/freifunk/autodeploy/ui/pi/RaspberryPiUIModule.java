package net.freifunk.autodeploy.ui.pi;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.autodeploy.ui.pi.peripherial.GroveSerialLCDDriverImpl;
import net.freifunk.autodeploy.ui.pi.peripherial.LCDDriver;

import com.google.inject.AbstractModule;

public class RaspberryPiUIModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LCDDriver.class).to(GroveSerialLCDDriverImpl.class).in(SINGLETON);
    }
}
