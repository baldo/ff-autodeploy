package net.freifunk.autodeploy.ui.pi;

import static com.google.inject.Scopes.SINGLETON;
import net.freifunk.autodeploy.AutoDeployModule;
import net.freifunk.autodeploy.printing.LabelPrintingService;
import net.freifunk.autodeploy.printing.LabelPrintingServiceImpl;
import net.freifunk.autodeploy.ui.pi.peripherals.GPIOJoystickDriverImpl;
import net.freifunk.autodeploy.ui.pi.peripherals.GroveSerialLCDDriverImpl;
import net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver;
import net.freifunk.autodeploy.ui.pi.peripherals.LCDDriver;

import com.google.inject.AbstractModule;

/**
 * Module for binding the Raspberry PI UI.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class RaspberryPiUIModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AutoDeployModule());
        bind(JoystickDriver.class).to(GPIOJoystickDriverImpl.class).in(SINGLETON);
        bind(LCDDriver.class).to(GroveSerialLCDDriverImpl.class).in(SINGLETON);
        bind(LabelPrintingService.class).to(LabelPrintingServiceImpl.class).in(SINGLETON);
    }
}
