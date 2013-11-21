package net.freifunk.autodeploy.ui.pi;

import static java.util.concurrent.TimeUnit.SECONDS;
import net.freifunk.autodeploy.ui.pi.peripherals.JoystickDriver;
import net.freifunk.autodeploy.ui.pi.peripherals.JoystickListener;
import net.freifunk.autodeploy.ui.pi.peripherals.LCDDriver;

import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * Main class for running the auto deployer on the Raspberry Pi.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class RaspberryPiMain {

    @Inject private JoystickDriver _buttonDriver;
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
            _lcdDriver.writeString("Test");

            _buttonDriver.listen(new JoystickListener() {

                @Override
                public void up() {
                    _lcdDriver.writeString("Up...");
                }

                @Override
                public void down() {
                    _lcdDriver.writeString("Down...");
                }

                @Override
                public void left() {
                    _lcdDriver.writeString("Left...");
                }

                @Override
                public void right() {
                    _lcdDriver.writeString("Right...");
                }

                @Override
                public void button() {
                    _lcdDriver.writeString("Button...");
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
