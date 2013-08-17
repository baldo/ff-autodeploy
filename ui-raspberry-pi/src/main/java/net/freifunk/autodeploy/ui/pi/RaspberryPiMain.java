package net.freifunk.autodeploy.ui.pi;

import static java.util.concurrent.TimeUnit.SECONDS;
import net.freifunk.autodeploy.ui.pi.peripherals.ButtonDriver;
import net.freifunk.autodeploy.ui.pi.peripherals.ButtonListener;
import net.freifunk.autodeploy.ui.pi.peripherals.LCDDriver;

import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Main class for running the auto deployer on the Raspberry Pi.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class RaspberryPiMain {

    @Inject private ButtonDriver _buttonDriver;
    @Inject private LCDDriver _lcdDriver;

    public RaspberryPiMain() {
        Guice.createInjector(new RaspberryPiUIModule()).injectMembers(this);
    }

    public static void main(final String args[]) throws InterruptedException {
        new RaspberryPiMain().run(args);
    }

    private void run(final String[] args) {
        try {
            _buttonDriver.init();
            _lcdDriver.init();

            _buttonDriver.listen(new ButtonListener() {

                @Override
                public void pressed() {
                    _lcdDriver.writeString("Pressed...");
                }

                @Override
                public void released() {
                    _lcdDriver.writeString("Released...");
                }
            });

            try {
                SECONDS.sleep(60);
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Interrupted.", e);
            }
        }
        finally {
            try {
                _buttonDriver.shutdown();
            }
            finally {
                _lcdDriver.shutdown();
            }
        }
    }
}
