package net.freifunk.autodeploy.ui.pi;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.autodeploy.ui.pi.peripherals.ButtonDriver;
import net.freifunk.autodeploy.ui.pi.peripherals.GPIOButtonDriverImpl;
import net.freifunk.autodeploy.ui.pi.peripherals.GroveSerialLCDDriverImpl;
import net.freifunk.autodeploy.ui.pi.peripherals.LCDDriver;

import com.google.inject.AbstractModule;

public class RaspberryPiUIModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ButtonDriver.class).to(GPIOButtonDriverImpl.class).in(SINGLETON);
        bind(LCDDriver.class).to(GroveSerialLCDDriverImpl.class).in(SINGLETON);
    }
}